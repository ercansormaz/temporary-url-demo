package dev.ercan.poc.temporaryurldemo.service;

import dev.ercan.poc.temporaryurldemo.model.TemporaryUrlRequest;
import dev.ercan.poc.temporaryurldemo.model.TemporaryUrlResponse;
import dev.ercan.poc.temporaryurldemo.util.HMacUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class TemporaryUrlService {

  private static final String EXPIRES = "exp";
  private static final String SIGNATURE = "sig";
  private static final String HMAC_BODY = "%s\n%s\n%s";

  @Value("${temp.url.secret}")
  private String secret;

  @Value("${temp.url.protected.base}")
  private String protectedBase;

  public TemporaryUrlResponse createTemporaryUrl(TemporaryUrlRequest request)
      throws NoSuchAlgorithmException, InvalidKeyException {
    long expires = Instant.now().plusSeconds(request.expiresIn()).toEpochMilli();
    String path = protectedBase + request.id();

    String signature = calculateSignature(request.method().name(), path, expires);

    String temporaryUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path(path)
        .queryParam(getSignatureParam(), signature)
        .queryParam(getExpiresParam(), expires)
        .toUriString();

    return new TemporaryUrlResponse(temporaryUrl);
  }

  public String calculateSignature(String method, String path, long expires)
      throws NoSuchAlgorithmException, InvalidKeyException {
    String hmacBody = String.format(HMAC_BODY, method, expires, path);
    return HMacUtil.hmac(hmacBody, secret);
  }

  public String getExpiresParam() {
    return EXPIRES;
  }

  public String getSignatureParam() {
    return SIGNATURE;
  }

  public String getProtectedBase() {
    return protectedBase;
  }

}
