package dev.ercan.poc.temporaryurldemo.interceptor;

import dev.ercan.poc.temporaryurldemo.exception.InvalidTemporaryUrlException;
import dev.ercan.poc.temporaryurldemo.service.TemporaryUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TemporaryUrlInterceptor Tests")
class TemporaryUrlInterceptorTest {

  private TemporaryUrlInterceptor interceptor;
  private TemporaryUrlService temporaryUrlService;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Object handler;

  private static final String SIGNATURE_PARAM = "sig";
  private static final String EXPIRES_PARAM = "exp";
  private static final String VALID_SIGNATURE = "valid-signature";

  @BeforeEach
  void setUp() {
    temporaryUrlService = mock(TemporaryUrlService.class);
    interceptor = new TemporaryUrlInterceptor(temporaryUrlService);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    handler = new Object();

    when(temporaryUrlService.getSignatureParam()).thenReturn(SIGNATURE_PARAM);
    when(temporaryUrlService.getExpiresParam()).thenReturn(EXPIRES_PARAM);
  }

  @Test
  @DisplayName("should allow request with valid signature and non-expired token")
  void shouldAllowRequestWithValidSignatureAndNonExpiredToken() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn("GET");
    when(temporaryUrlService.calculateSignature("GET", path, futureExpires)).thenReturn(VALID_SIGNATURE);

    boolean result = interceptor.preHandle(request, response, handler);

    assertTrue(result);
  }

  @Test
  @DisplayName("should reject request when signature parameter is missing")
  void shouldRejectRequestWhenSignatureParameterIsMissing() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(null);

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class, 
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("sig not present", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request when signature parameter is empty")
  void shouldRejectRequestWhenSignatureParameterIsEmpty() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn("");

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("sig not present", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request when signature parameter is whitespace")
  void shouldRejectRequestWhenSignatureParameterIsWhitespace() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn("   ");

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("sig not present", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request when expires parameter is missing")
  void shouldRejectRequestWhenExpiresParameterIsMissing() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(null);

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("exp not present", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request when expires parameter is empty")
  void shouldRejectRequestWhenExpiresParameterIsEmpty() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn("");

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("exp not present", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request when expires parameter is whitespace")
  void shouldRejectRequestWhenExpiresParameterIsWhitespace() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn("   ");

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("exp not present", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request with expired token")
  void shouldRejectRequestWithExpiredToken() {
    long pastExpires = Instant.now().minusSeconds(3600).toEpochMilli();

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(pastExpires));

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("Link expired", exception.getMessage());
  }

  @Test
  @DisplayName("should reject request with invalid signature")
  void shouldRejectRequestWithInvalidSignature() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource";
    String invalidSignature = "invalid-signature";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(invalidSignature);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn("GET");
    when(temporaryUrlService.calculateSignature("GET", path, futureExpires)).thenReturn(VALID_SIGNATURE);

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertEquals("sig not match", exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH"})
  @DisplayName("should work with different HTTP methods")
  void shouldWorkWithDifferentHttpMethods(String method) throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn(method);
    when(temporaryUrlService.calculateSignature(method, path, futureExpires)).thenReturn(VALID_SIGNATURE);

    boolean result = interceptor.preHandle(request, response, handler);

    assertTrue(result);
  }

  @Test
  @DisplayName("should use request URI and method for signature calculation")
  void shouldUseRequestUriAndMethodForSignatureCalculation() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource/123";
    String method = "POST";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn(method);
    when(temporaryUrlService.calculateSignature(method, path, futureExpires)).thenReturn(VALID_SIGNATURE);

    interceptor.preHandle(request, response, handler);

    verify(temporaryUrlService).calculateSignature(method, path, futureExpires);
  }

  @Test
  @DisplayName("should handle numeric overflow for expires parameter")
  void shouldHandleNumericOverflowForExpiresParameter() throws Exception {
    long largeExpires = Long.MAX_VALUE;

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(largeExpires));
    when(request.getRequestURI()).thenReturn("/protected/resource");
    when(request.getMethod()).thenReturn("GET");
    when(temporaryUrlService.calculateSignature(anyString(), anyString(), eq(largeExpires))).thenReturn(VALID_SIGNATURE);

    boolean result = interceptor.preHandle(request, response, handler);

    assertTrue(result);
  }

  @Test
  @DisplayName("should reject request with malformed expires parameter")
  void shouldRejectRequestWithMalformedExpiresParameter() {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn("not-a-number");

    assertThrows(NumberFormatException.class,
        () -> interceptor.preHandle(request, response, handler));
  }
}
