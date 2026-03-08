package com.manu.forecaster.service;

import com.manu.forecaster.exception.RestException;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.*;

class RestServiceTest {

    private RestService restService;

    @BeforeEach
    void setUp() {
        restService = new RestService();
    }

    // --- serialize tests ---

    @Test
    void serialize_simplePojo_returnsValidJson() throws RestException {
        TestPojo pojo = new TestPojo("hello", 42);
        String json = restService.serialize(pojo);
        assertTrue(json.contains("\"name\":\"hello\""));
        assertTrue(json.contains("\"value\":42"));
    }

    @Test
    void serialize_nullFields_returnsJsonWithNulls() throws RestException {
        TestPojo pojo = new TestPojo(null, 0);
        String json = restService.serialize(pojo);
        assertTrue(json.contains("\"name\":null"));
        assertTrue(json.contains("\"value\":0"));
    }

    // --- deserialize tests ---

    @Test
    void deserialize_validJson_returnsCorrectlyMappedPojo() throws RestException {
        String json = "{\"name\":\"hello\",\"value\":42}";
        TestPojo result = restService.deserialize(json, TestPojo.class);
        assertEquals("hello", result.getName());
        assertEquals(42, result.getValue());
    }

    @Test
    void deserialize_invalidJson_throwsRestException() {
        assertThrows(RestException.class, () -> restService.deserialize("not a json", TestPojo.class));
    }

    @Test
    void deserialize_jsonMissingFields_returnsPojoWithDefaults() throws RestException {
        String json = "{\"name\":\"hello\"}";
        TestPojo result = restService.deserialize(json, TestPojo.class);
        assertEquals("hello", result.getName());
        assertEquals(0, result.getValue());
    }

    // --- validateResponse (OkHttp) tests ---

    private Response buildOkHttpResponse(int code, String bodyContent) {
        Request request = new Request.Builder().url("https://example.com").build();
        ResponseBody body = bodyContent != null
                ? ResponseBody.create(bodyContent, MediaType.parse("application/json"))
                : null;
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("msg")
                .body(body)
                .build();
    }

    @Test
    void validateResponse_okhttp_successful_returnsBody() throws RestException {
        Response response = buildOkHttpResponse(200, "{\"ok\":true}");
        ResponseBody result = restService.validateResponse(response);
        assertNotNull(result);
    }

    @Test
    void validateResponse_okhttp_unsuccessful_throwsRestException() {
        Response response = buildOkHttpResponse(500, "error");
        assertThrows(RestException.class, () -> restService.validateResponse(response));
    }

    // --- validateResponse (HttpRequest/HttpResponse) tests ---

    private HttpRequest buildHttpRequest(URI uri) {
        return HttpRequest.newBuilder().uri(uri).GET().build();
    }

    private HttpResponse<String> buildHttpResponse(URI uri, int statusCode, String body) {
        return new HttpResponse<>() {
            @Override public int statusCode() { return statusCode; }
            @Override public HttpRequest request() { return null; }
            @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
            @Override public HttpHeaders headers() { return HttpHeaders.of(java.util.Map.of(), (a, b) -> true); }
            @Override public String body() { return body; }
            @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
            @Override public URI uri() { return uri; }
            @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        };
    }

    @Test
    void validateResponse_http_matchingUri200NonEmptyBody_noException() {
        URI uri = URI.create("https://example.com/api");
        HttpRequest request = buildHttpRequest(uri);
        HttpResponse<String> response = buildHttpResponse(uri, 200, "some body");
        assertDoesNotThrow(() -> restService.validateResponse(request, response));
    }

    @Test
    void validateResponse_http_mismatchedUris_throwsRestException() {
        HttpRequest request = buildHttpRequest(URI.create("https://example.com/api"));
        HttpResponse<String> response = buildHttpResponse(URI.create("https://other.com/api"), 200, "body");
        assertThrows(RestException.class, () -> restService.validateResponse(request, response));
    }

    @Test
    void validateResponse_http_statusCode404_throwsRestException() {
        URI uri = URI.create("https://example.com/api");
        HttpRequest request = buildHttpRequest(uri);
        HttpResponse<String> response = buildHttpResponse(uri, 404, "not found");
        assertThrows(RestException.class, () -> restService.validateResponse(request, response));
    }

    @Test
    void validateResponse_http_status200EmptyBody_throwsRestException() {
        URI uri = URI.create("https://example.com/api");
        HttpRequest request = buildHttpRequest(uri);
        HttpResponse<String> response = buildHttpResponse(uri, 200, "");
        assertThrows(RestException.class, () -> restService.validateResponse(request, response));
    }

    @Test
    void validateResponse_http_status201_noException() {
        URI uri = URI.create("https://example.com/api");
        HttpRequest request = buildHttpRequest(uri);
        HttpResponse<String> response = buildHttpResponse(uri, 201, "created");
        assertDoesNotThrow(() -> restService.validateResponse(request, response));
    }

    // --- Test POJO ---

    static class TestPojo {
        private String name;
        private int value;
        public TestPojo() {}
        public TestPojo(String name, int value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}
