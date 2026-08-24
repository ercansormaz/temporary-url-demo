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
  void shouldRejectRequestWhenSignatureParameterIsMissing() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(null);

    assertThrows(InvalidTemporaryUrlException.class, 
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should reject request when signature parameter is empty")
  void shouldRejectRequestWhenSignatureParameterIsEmpty() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn("");

    assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should reject request when signature parameter is whitespace")
  void shouldRejectRequestWhenSignatureParameterIsWhitespace() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn("   ");

    assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should reject request when expires parameter is missing")
  void shouldRejectRequestWhenExpiresParameterIsMissing() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(null);

    assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should reject request when expires parameter is empty")
  void shouldRejectRequestWhenExpiresParameterIsEmpty() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn("");

    assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should reject request when expires parameter is whitespace")
  void shouldRejectRequestWhenExpiresParameterIsWhitespace() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn("   ");

    assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should reject request with expired token")
  void shouldRejectRequestWithExpiredToken() throws Exception {
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

  @Test
  @DisplayName("should reject request when calculated signature does not match provided signature")
  void shouldRejectRequestWhenCalculatedSignatureDoesNotMatch() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn("wrong-signature");
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn("GET");
    when(temporaryUrlService.calculateSignature("GET", path, futureExpires)).thenReturn("correct-signature");

    assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));
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
  @DisplayName("should use request URI for signature calculation")
  void shouldUseRequestUriForSignatureCalculation() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource/123";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn("GET");
    when(temporaryUrlService.calculateSignature("GET", path, futureExpires)).thenReturn(VALID_SIGNATURE);

    interceptor.preHandle(request, response, handler);

    verify(temporaryUrlService).calculateSignature("GET", path, futureExpires);
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
  void shouldRejectRequestWithMalformedExpiresParameter() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn("not-a-number");

    assertThrows(NumberFormatException.class,
        () -> interceptor.preHandle(request, response, handler));
  }

  @Test
  @DisplayName("should verify signature with correct method and path")
  void shouldVerifySignatureWithCorrectMethodAndPath() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/myresource";
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
  @DisplayName("should throw exception with correct message when signature parameter missing")
  void shouldThrowExceptionWithCorrectMessageWhenSignatureParameterMissing() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(null);

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertTrue(exception.getMessage().contains("sig"));
    assertTrue(exception.getMessage().contains("not present"));
  }

  @Test
  @DisplayName("should throw exception with correct message when expires parameter missing")
  void shouldThrowExceptionWithCorrectMessageWhenExpiresParameterMissing() throws Exception {
    when(request.getParameter(SIGNATURE_PARAM)).thenReturn(VALID_SIGNATURE);
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(null);

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertTrue(exception.getMessage().contains("exp"));
    assertTrue(exception.getMessage().contains("not present"));
  }

  @Test
  @DisplayName("should throw exception with correct message when signature not match")
  void shouldThrowExceptionWithCorrectMessageWhenSignatureNotMatch() throws Exception {
    long futureExpires = Instant.now().plusSeconds(3600).toEpochMilli();
    String path = "/protected/resource";

    when(request.getParameter(SIGNATURE_PARAM)).thenReturn("wrong");
    when(request.getParameter(EXPIRES_PARAM)).thenReturn(String.valueOf(futureExpires));
    when(request.getRequestURI()).thenReturn(path);
    when(request.getMethod()).thenReturn("GET");
    when(temporaryUrlService.calculateSignature("GET", path, futureExpires)).thenReturn("correct");

    InvalidTemporaryUrlException exception = assertThrows(InvalidTemporaryUrlException.class,
        () -> interceptor.preHandle(request, response, handler));

    assertTrue(exception.getMessage().contains("sig"));
    assertTrue(exception.getMessage().contains("not match"));
  }
}
