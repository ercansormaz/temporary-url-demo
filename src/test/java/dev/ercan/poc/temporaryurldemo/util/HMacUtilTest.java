package dev.ercan.poc.temporaryurldemo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HMacUtil Tests")
class HMacUtilTest {

  private static final String TEST_KEY = "my-secret-key";
  private static final String TEST_DATA = "GET\n1692921600000\n/protected/resource";

  @Test
  @DisplayName("should reflect private constructor")
  void shouldReflectPrivateConstructor() throws Exception {
    var constructor = HMacUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }

  @Test
  @DisplayName("should generate valid HMAC for given data and key")
  void shouldGenerateValidHmac() throws NoSuchAlgorithmException, InvalidKeyException {
    String result = HMacUtil.hmac(TEST_DATA, TEST_KEY);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(isValidBase64Url(result));
  }

  @Test
  @DisplayName("should generate same HMAC for identical inputs")
  void shouldGenerateSameHmacForIdenticalInputs() throws NoSuchAlgorithmException, InvalidKeyException {
    String hmac1 = HMacUtil.hmac(TEST_DATA, TEST_KEY);
    String hmac2 = HMacUtil.hmac(TEST_DATA, TEST_KEY);

    assertEquals(hmac1, hmac2);
  }

  @Test
  @DisplayName("should generate different HMAC for different data")
  void shouldGenerateDifferentHmacForDifferentData() throws NoSuchAlgorithmException, InvalidKeyException {
    String data1 = "GET\n1692921600000\n/protected/resource";
    String data2 = "POST\n1692921600000\n/protected/resource";

    String hmac1 = HMacUtil.hmac(data1, TEST_KEY);
    String hmac2 = HMacUtil.hmac(data2, TEST_KEY);

    assertNotEquals(hmac1, hmac2);
  }

  @Test
  @DisplayName("should generate different HMAC for different keys")
  void shouldGenerateDifferentHmacForDifferentKeys() throws NoSuchAlgorithmException, InvalidKeyException {
    String key1 = "secret-key-1";
    String key2 = "secret-key-2";

    String hmac1 = HMacUtil.hmac(TEST_DATA, key1);
    String hmac2 = HMacUtil.hmac(TEST_DATA, key2);

    assertNotEquals(hmac1, hmac2);
  }

  @Test
  @DisplayName("should handle empty data string")
  void shouldHandleEmptyData() throws NoSuchAlgorithmException, InvalidKeyException {
    String result = HMacUtil.hmac("", TEST_KEY);

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  @DisplayName("should handle empty key string")
  void shouldHandleEmptyKey() {
    assertThrows(IllegalArgumentException.class, 
        () -> HMacUtil.hmac(TEST_DATA, ""));
  }

  @Test
  @DisplayName("should handle special characters in data")
  void shouldHandleSpecialCharactersInData() throws NoSuchAlgorithmException, InvalidKeyException {
    String dataWithSpecialChars = "GET\n1692921600000\n/protected/resource?param=value&other=123";

    String result = HMacUtil.hmac(dataWithSpecialChars, TEST_KEY);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(isValidBase64Url(result));
  }

  @Test
  @DisplayName("should handle UTF-8 characters")
  void shouldHandleUtf8Characters() throws NoSuchAlgorithmException, InvalidKeyException {
    String dataWithUtf8 = "GET\n1692921600000\n/protected/türkçe/kaynak";

    String result = HMacUtil.hmac(dataWithUtf8, TEST_KEY);

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "GET\n1692921600000\n/protected/resource",
      "POST\n1692921600000\n/protected/resource",
      "PUT\n1692921600000\n/protected/resource",
      "DELETE\n1692921600000\n/protected/resource"
  })
  @DisplayName("should work with different HTTP methods")
  void shouldWorkWithDifferentHttpMethods(String data) throws NoSuchAlgorithmException, InvalidKeyException {
    String result = HMacUtil.hmac(data, TEST_KEY);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(isValidBase64Url(result));
  }

  @Test
  @DisplayName("should produce URL-safe base64 encoding without padding")
  void shouldProduceUrlSafeBase64WithoutPadding() throws NoSuchAlgorithmException, InvalidKeyException {
    String result = HMacUtil.hmac(TEST_DATA, TEST_KEY);

    assertFalse(result.contains("+"), "Result should not contain '+' character");
    assertFalse(result.contains("/"), "Result should not contain '/' character");
    assertFalse(result.contains("="), "Result should not contain '=' padding");
  }

  private boolean isValidBase64Url(String str) {
    try {
      Base64.getUrlDecoder().decode(str);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
