package dev.ercan.poc.temporaryurldemo.model;

import org.springframework.http.HttpMethod;

public record TemporaryUrlRequest(HttpMethod method, String id, Long expiresIn) {

}
