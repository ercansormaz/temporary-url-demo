package dev.ercan.poc.temporaryurldemo.controller;

import dev.ercan.poc.temporaryurldemo.model.TemporaryUrlRequest;
import dev.ercan.poc.temporaryurldemo.model.TemporaryUrlResponse;
import dev.ercan.poc.temporaryurldemo.service.TemporaryUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class TemporaryUrlController {

  private final TemporaryUrlService temporaryUrlService;

  @PostMapping("/temporary-url")
  public TemporaryUrlResponse createTemporaryUrl(@RequestBody TemporaryUrlRequest request)
      throws NoSuchAlgorithmException, InvalidKeyException {
    return temporaryUrlService.createTemporaryUrl(request);
  }

}
