# Temporary URL Demo

This project demonstrates a **Temporary URL mechanism** implemented in Spring Boot.  
It allows generating signed URLs with expiration time and validates them before accessing protected resources.

---

## ✨ Features
- Generate temporary URLs with HMAC-SHA256 signatures.
- Interceptor-based validation for all `/protected/**` endpoints.
- Automatic rejection with **403 Forbidden** if:
  - `sig` or `exp` parameters are missing,
  - Expiration time has passed,
  - Signature does not match.
- Supports multiple HTTP methods:
  - `GET /protected/{id}`
  - `POST /protected/{id}`
  - `PUT /protected/{id}`
  - `PATCH /protected/{id}`
  - `DELETE /protected/{id}`

---

## 🚀 Generate Temporary URL
Use the `/temporary-url` endpoint to create a signed URL.

**Example Request:**
```bash
curl --request POST \
  --url http://localhost:8080/temporary-url \
  --header 'Content-Type: application/json' \
  --data '{
  "method": "POST",
  "id": "123123123",
  "expiresIn": 60
}'
```

**Example Response:**
```json
{
  "url": "http://localhost:8080/protected/123123123?sig=_Vix2IsIUA-F0erIr056IdwPKSKe04Yo_DwCxC4irts&exp=1787582041957"
}
```
---

## 🛡️ Access Protected Resources

Once you have a valid temporary URL, you can call the protected endpoint:
```bash
curl --request POST \
  --url "http://localhost:8080/protected/123123123?sig=...&exp=..."
```
For demonstration purposes, successful requests return a simple message like:

```text
POST Request Allowed for 123123123
```

If the signature is invalid or expired, the server responds with:
```text
HTTP/1.1 403 Forbidden
```
---

## 📚 Use Cases
- Temporary access to protected APIs.
- Time-limited resource sharing.

---

## ⚖️ License
This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for details.
