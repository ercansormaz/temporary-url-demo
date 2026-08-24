package dev.ercan.poc.temporaryurldemo.handler;

import dev.ercan.poc.temporaryurldemo.exception.InvalidTemporaryUrlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(InvalidTemporaryUrlException.class)
  public ResponseEntity<?> handleInvalidTemporaryUrlException(InvalidTemporaryUrlException ex) {
    log.info("[InvalidTempUrlException] [Message={}]", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

}
