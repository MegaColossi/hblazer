package dev.edumelo.hblazer.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class AsyncJobsManager {
	
	private Logger log = LoggerFactory.getLogger(AsyncJobsManager.class);
	
	private static final String JOB_WITH_SUPPLIED_JOB_ID_NOT_FOUND =
			"Job with supplied job-id not found";
	private final MqttService mqttService;
	private Long defaultJobExpiration;
	private Long posCompletionJobExpiration;
	private final ExpiringMap<String, CompletableFuture<? extends BaseResponse>> mapOfJobs =
			ExpiringMap.builder().variableExpiration().build();
	
	public AsyncJobsManager(Long defaultJobExpiration, Long posCompletionJobExpiration,
			RestTemplate restTemplate, String mqttServerAddress, String mqttJwtToken) {
		this.defaultJobExpiration = defaultJobExpiration;
		this.posCompletionJobExpiration = posCompletionJobExpiration;
		this.mqttService = new MqttService(restTemplate, mqttServerAddress, mqttJwtToken);
	}
	
	public void putNotifierJob(String jobIdentifier, String jobId, Long jobExpiration,
			CompletableFuture<? extends BaseResponse> job, long tokenValidityInMilliseconds,
			Object... inputParameters) {
		job.whenComplete((response, exception) -> {
			 if (exception == null) {
				 mqttService.publish(response, jobExpiration, tokenValidityInMilliseconds,
						 jobIdentifier);
             } else {
            	 mqttService.publishException(inputParameters, exception.getMessage(),
            			 jobExpiration, tokenValidityInMilliseconds, jobIdentifier);
             }
		});
		mapOfJobs.put(jobId, job, ExpirationPolicy.CREATED, defaultJobExpiration,
				TimeUnit.SECONDS);
	}
	
	public void putJob(String jobId, CompletableFuture<? extends BaseResponse> job) {
		mapOfJobs.put(jobId, job, ExpirationPolicy.CREATED, defaultJobExpiration,
				TimeUnit.SECONDS);
	}

	public CompletableFuture<? extends BaseResponse> getJob(String jobId) {
		return mapOfJobs.get(jobId);
	}

	public void removeJob(String jobId) {
		mapOfJobs.remove(jobId);
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<SimpleResponse<?>> fetchJob(String jobId) {
		CompletableFuture<SimpleResponse<?>> completableFuture =
		(CompletableFuture<SimpleResponse<?>>) getJob(jobId);

		return completableFuture;
	}
	
	public CompletableFuture<SimpleResponse<?>> fetchJobElseThrowException(String jobId) {
		CompletableFuture<SimpleResponse<?>> job = fetchJob(jobId);
		if(null == job) {
			log.error("Job-id {} not found.", jobId);
			throw new RuntimeException(JOB_WITH_SUPPLIED_JOB_ID_NOT_FOUND);
		}
		return job;
	}

	public SimpleResponse<?> getJobStatus(String jobId) throws Throwable {
		CompletableFuture<SimpleResponse<?>> future = fetchJobElseThrowException(jobId);
		if(!future.isDone()) {
			return new SimpleResponse<>(jobId, RequestStatus.IN_PROGRESS);
		}
		
		Throwable[] errors = new Throwable[1];
		SimpleResponse<?>[] simpleResponses = new SimpleResponse[1];
		future.whenComplete((response, ex) -> {
			if (ex != null) {
				errors[0] = ex.getCause();
			} else {
				simpleResponses[0] = response;
			}
		});

		if (errors[0] != null) {
			throw errors[0];
		}

		return simpleResponses[0];
	}

	public Object getValue(String jobId) throws Throwable {
		CompletableFuture<SimpleResponse<?>> completableFuture = fetchJob(jobId);

		if (null == completableFuture) {
			throw new RuntimeException(JOB_WITH_SUPPLIED_JOB_ID_NOT_FOUND);
		}

		if (!completableFuture.isDone()) {
			throw new RuntimeException("Job is still in progress...");
		}

		Throwable[] errors = new Throwable[1];
		SimpleResponse<?>[] simpleResponses = new SimpleResponse[1];
		completableFuture.whenComplete((response, ex) -> {
			if (ex != null) {
				errors[0] = ex.getCause();
			} else {
				simpleResponses[0] = response;
			}
		});

		if (errors[0] != null) {
			throw errors[0];
		}

		return simpleResponses[0].getValue();
	}

	public void startExpiration(String jobId) {
		mapOfJobs.setExpiration(jobId, posCompletionJobExpiration, TimeUnit.SECONDS);
	}
	
	public <T> CompletableFuture<SimpleResponse<T>> wrapByJob(String jobId, T content) {
		Assert.notNull(jobId, "Parameter jobId should not be null");
        CompletableFuture<SimpleResponse<T>> task = new CompletableFuture<>();
        task.complete(new SimpleResponse<>(jobId, RequestStatus.COMPLETE, content));
		return task;
	}

	public <T> CompletableFuture<SimpleResponse<T>> wrapByJob(String jobIdentifier, String jobId,
			Long jobExpiration, T content) {
		CompletableFuture<SimpleResponse<T>> task = wrapByJob(jobId, content);
		return task;
	}
	
}
