package dev.edumelo.hblazer.async;

public class MqttExceptionContent {
	String jobId;
	int failCount;
	String exceptionMessage;
	Object[] content;
	
	public MqttExceptionContent(String jobId, int failCount, String exceptionMessage,
			Object[] content) {
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

	public Object[] getContent() {
		return content;
	}
	
}
