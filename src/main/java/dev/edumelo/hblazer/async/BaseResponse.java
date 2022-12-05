package dev.edumelo.hblazer.async;

public class BaseResponse {
	
	String jobId;
	RequestStatus status;
	
	public BaseResponse(String jobId, RequestStatus status) {
		super();
		this.jobId = jobId;
		this.status = status;
	}

	public String getJobId() {
		return jobId;
	}

	public RequestStatus getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "BaseResponse [jobId=" + jobId + ", status=" + status + "]";
	}

}
