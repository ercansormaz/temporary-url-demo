package dev.ercan.poc.temporaryurldemo.interceptor;

import dev.ercan.poc.temporaryurldemo.exception.InvalidTemporaryUrlException;
import dev.ercan.poc.temporaryurldemo.service.TemporaryUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.Instant;

@RequiredArgsConstructor
public class TemporaryUrlInterceptor implements HandlerInterceptor {

  private final TemporaryUrlService temporaryUrlService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    String signatureParam = request.getParameter(temporaryUrlService.getSignatureParam());

    if (!StringUtils.hasText(signatureParam)) {
      throw new InvalidTemporaryUrlException(temporaryUrlService.getSignatureParam() + " not present");
    }

    String expiresParam = request.getParameter(temporaryUrlService.getExpiresParam());

    if (!StringUtils.hasText(expiresParam)) {
      throw new InvalidTemporaryUrlException(temporaryUrlService.getExpiresParam() + " not present");
    }

    long expires = Long.parseLong(expiresParam);

    if (Instant.now().toEpochMilli() > expires) {
      throw new InvalidTemporaryUrlException("Link expired");
    }

    String path = request.getRequestURI();

    String calculatedSignature = temporaryUrlService.calculateSignature(request.getMethod(), path, expires);

    if (!calculatedSignature.equals(signatureParam)) {
      throw new InvalidTemporaryUrlException(temporaryUrlService.getSignatureParam() + " not match");
    }

    return true;
  }

}
