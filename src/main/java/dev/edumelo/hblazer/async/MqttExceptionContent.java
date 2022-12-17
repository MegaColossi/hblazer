package dev.edumelo.hblazer.async;

import java.util.Map;

public class MqttExceptionContent {
	String jobId;
	int failCount;
	String exceptionMessage;
	Map<String, Object> content;
	
	public MqttExceptionContent(String jobId, int failCount, String exceptionMessage,
			Map<String, Object> content) {
		this.jobId = jobId;
		this.failCount = failCount;
		this.exceptionMessage = exceptionMessage;
		this.content = content;
	}
	
	public String getJobId() {
		return jobId;
	}

	public int getFailCount() {
		return failCount;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public Map<String, Object> getContent() {
		return content;
	}
	
}
