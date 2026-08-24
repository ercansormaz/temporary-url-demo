package dev.ercan.poc.temporaryurldemo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TemporaryUrlService Tests")
class TemporaryUrlServiceTest {

  private TemporaryUrlService service;
  private static final String SECRET_KEY = "test-secret-key";
  private static final String PROTECTED_BASE = "/protected";

  @BeforeEach
  void setUp() {
    service = new TemporaryUrlService();
    ReflectionTestUtils.setField(service, "secret", SECRET_KEY);
    ReflectionTestUtils.setField(service, "protectedBase", PROTECTED_BASE);
  }

  @Test
  @DisplayName("should calculate signature correctly")
  void shouldCalculateSignatureCorrectly() throws NoSuchAlgorithmException, InvalidKeyException {
    String method = "GET";
    String path = "/protected/resource";
    long expires = 1692921600000L;

    String signature = service.calculateSignature(method, path, expires);

    assertNotNull(signature);
    assertFalse(signature.isEmpty());
    assertFalse(signature.contains("+"));
    assertFalse(signature.contains("/"));
  }

  @Test
  @DisplayName("should generate different signatures for different methods")
  void shouldGenerateDifferentSignaturesForDifferentMethods() throws NoSuchAlgorithmException, InvalidKeyException {
    String path = "/protected/resource";
    long expires = 1692921600000L;

    String getSig = service.calculateSignature("GET", path, expires);
    String postSig = service.calculateSignature("POST", path, expires);

    assertNotEquals(getSig, postSig);
  }

  @Test
  @DisplayName("should generate different signatures for different paths")
  void shouldGenerateDifferentSignaturesForDifferentPaths() throws NoSuchAlgorithmException, InvalidKeyException {
    String method = "GET";
    long expires = 1692921600000L;

    String sig1 = service.calculateSignature(method, "/protected/resource1", expires);
    String sig2 = service.calculateSignature(method, "/protected/resource2", expires);

    assertNotEquals(sig1, sig2);
  }

  @Test
  @DisplayName("should generate different signatures for different expiration times")
  void shouldGenerateDifferentSignaturesForDifferentExpirations() throws NoSuchAlgorithmException, InvalidKeyException {
    String method = "GET";
    String path = "/protected/resource";

    String sig1 = service.calculateSignature(method, path, 1692921600000L);
    String sig2 = service.calculateSignature(method, path, 1692921700000L);

    assertNotEquals(sig1, sig2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH"})
  @DisplayName("should work with different HTTP methods")
  void shouldWorkWithDifferentHttpMethods(String method) throws NoSuchAlgorithmException, InvalidKeyException {
    String signature = service.calculateSignature(method, "/protected/resource", System.currentTimeMillis());

    assertNotNull(signature);
    assertFalse(signature.isEmpty());
  }

  @Test
  @DisplayName("should return consistent signature parameter name")
  void shouldReturnConsistentSignatureParamName() {
    String sigParam1 = service.getSignatureParam();
    String sigParam2 = service.getSignatureParam();

    assertEquals(sigParam1, sigParam2);
    assertEquals("sig", sigParam1);
  }

  @Test
  @DisplayName("should return consistent expires parameter name")
  void shouldReturnConsistentExpiresParamName() {
    String expParam1 = service.getExpiresParam();
    String expParam2 = service.getExpiresParam();

    assertEquals(expParam1, expParam2);
    assertEquals("exp", expParam1);
  }

  @Test
  @DisplayName("should return protected base path")
  void shouldReturnProtectedBasePath() {
    String basePath = service.getProtectedBase();

    assertNotNull(basePath);
    assertEquals(PROTECTED_BASE, basePath);
  }

  @Test
  @DisplayName("should include path in signature calculation")
  void shouldIncludePathInSignatureCalculation() throws NoSuchAlgorithmException, InvalidKeyException {
    String method = "GET";
    long expires = 1692921600000L;

    String sig1 = service.calculateSignature(method, "/protected/resource1", expires);
    String sig2 = service.calculateSignature(method, "/protected/resource2", expires);

    assertNotEquals(sig1, sig2);
  }

  @Test
  @DisplayName("should verify signature with correct method and path")
  void shouldVerifySignatureWithCorrectMethodAndPath() throws NoSuchAlgorithmException, InvalidKeyException {
    String path = "/protected/myresource";
    String method = "POST";
    long expires = 1692921600000L;

    String signature = service.calculateSignature(method, path, expires);

    assertNotNull(signature);
    assertFalse(signature.isEmpty());
  }

  @Test
  @DisplayName("should generate same signature for identical inputs")
  void shouldGenerateSameSignatureForIdenticalInputs() throws NoSuchAlgorithmException, InvalidKeyException {
    String method = "GET";
    String path = "/protected/resource";
    long expires = 1692921600000L;

    String sig1 = service.calculateSignature(method, path, expires);
    String sig2 = service.calculateSignature(method, path, expires);

    assertEquals(sig1, sig2);
  }

  @Test
  @DisplayName("should handle special characters in path")
  void shouldHandleSpecialCharactersInPath() throws NoSuchAlgorithmException, InvalidKeyException {
    String method = "GET";
    String pathWithParams = "/protected/resource?id=123&name=test";
    long expires = 1692921600000L;

    String signature = service.calculateSignature(method, pathWithParams, expires);

    assertNotNull(signature);
    assertFalse(signature.isEmpty());
  }

  @Test
  @DisplayName("should produce URL-safe base64 signature")
  void shouldProduceUrlSafeBase64Signature() throws NoSuchAlgorithmException, InvalidKeyException {
    String signature = service.calculateSignature("GET", "/protected/resource", System.currentTimeMillis());

    assertFalse(signature.contains("+"), "Signature should not contain '+'");
    assertFalse(signature.contains("/"), "Signature should not contain '/'");
    assertFalse(signature.contains("="), "Signature should not contain padding");
  }
}
