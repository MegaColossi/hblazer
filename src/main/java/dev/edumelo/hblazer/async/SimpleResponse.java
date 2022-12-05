package dev.edumelo.hblazer.async;

import java.util.Objects;

public class SimpleResponse<T> extends BaseResponse {
	
	private T value;
	
	public SimpleResponse(String jobId, RequestStatus status) {
		super(jobId, status);
	}

	public SimpleResponse(String jobId, RequestStatus status, T value) {
		super(jobId, status);
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleResponse other = (SimpleResponse) obj;
		return Objects.equals(value, other.value);
	}
	
}
