package dev.edumelo.hblazer.async;

public class MqttExceptionContent {
	String exceptionMessage;
	Object[] content;
	
	public MqttExceptionContent(String exceptionMessage, Object[] content) {
		this.exceptionMessage = exceptionMessage;
		this.content = content;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public Object[] getContent() {
		return content;
	}
	
}
