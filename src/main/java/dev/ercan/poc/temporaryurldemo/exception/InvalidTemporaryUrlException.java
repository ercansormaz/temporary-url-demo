package dev.ercan.poc.temporaryurldemo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidTemporaryUrlException extends RuntimeException {

  private final String message;

}
