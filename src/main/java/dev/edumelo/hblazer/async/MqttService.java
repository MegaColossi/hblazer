package dev.edumelo.hblazer.async;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.util.Date;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import dev.edumelo.hblazer.utils.ArrayUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class MqttService {
	private static final String AUTHORITIES_KEY = "auth";
	private final RestTemplate rest;
	private final String mqttServerAddress;
	private final String mqttJwtToken;
	
	public MqttService(RestTemplate rest, String mqttServerAddress, String mqttJwtToken) {
		this.rest = rest;
		this.mqttServerAddress = mqttServerAddress;
		this.mqttJwtToken = mqttJwtToken;
	}

	public <T> String publish(T content, long expiration, long tokenValidityInMilliseconds,
			String... topicParts) {
		try {
			URI url = getApiUrl(mqttServerAddress, expiration, topicParts);
			ResponseEntity<String> response = rest.postForEntity(url, requestEntity(content,
					tokenValidityInMilliseconds),
					String.class);
			return response.getBody();			
		} catch(URISyntaxException e) {
			throw new RuntimeException("It wasn't possible to publish to mqtt", e);
		}
	}
	
	public String publishException(Object[] content, String exceptionMessage, Long expiration,
			long tokenValidityInMilliseconds, String jobId, int failCount, String... topicParts) {
		MqttExceptionContent exceptionContent = new MqttExceptionContent(jobId, failCount,
				exceptionMessage, content);
		try {
			String[] exceptionTopic = ArrayUtils.concatWithCollection(new String[]{"exception"},
					topicParts);
			URI url = getApiUrl(mqttServerAddress, expiration, exceptionTopic);
			ResponseEntity<String> response = rest.postForEntity(url,
					requestEntity(exceptionContent, tokenValidityInMilliseconds), String.class);
			return response.getBody();			
		} catch(URISyntaxException e) {
			throw new RuntimeException("It wasn't possible to publish to mqtt", e);
		}
	}

	private URI getApiUrl(String mqttServerAddress, Long expiration, String[] topicParts)
			throws URISyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(mqttServerAddress);
		
		for (int i = 0; i < topicParts.length; i++) {
			sb.append(topicParts[i]);
			if(i < topicParts.length-1) {
				sb.append(",");					
			}
		}
		
		sb.append("/message");
		sb.append("?expiration=");
		sb.append(expiration);
		return new URI(sb.toString());
	}
	
	private <B> HttpEntity<B> requestEntity(B body, long tokenValidityInMilliseconds) {
		HttpHeaders headers = new HttpHeaders();
		byte[] keyBytes = Decoders.BASE64.decode(mqttJwtToken);
		Key key = Keys.hmacShaKeyFor(keyBytes);
		long now = (new Date()).getTime();
		Date validity = new Date(now + tokenValidityInMilliseconds);
		String auth = Jwts
		        .builder()
		        .setSubject("user")
		        .claim(AUTHORITIES_KEY, "role")
		        .signWith(key, SignatureAlgorithm.HS512)
		        .setExpiration(validity)
		        .compact();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + auth);
		return new HttpEntity<>(body, headers);
	}

}
