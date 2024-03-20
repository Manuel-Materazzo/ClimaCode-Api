package com.manu.forecaster.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.manu.forecaster.exception.RestException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Scope("singleton")
public class RestService {

    private static final OkHttpClient CLIENT = new OkHttpClient().newBuilder().build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public RestService() {
        MAPPER.registerModule(new JavaTimeModule());
    }

    public synchronized Response executeRequest(Request request) throws IOException {
        return CLIENT.newCall(request).execute();
    }

    /**
     * Validates the rest template response against the request
     *
     * @param request
     * @param response
     */
    public void validateResponse(HttpRequest request, HttpResponse<String> response) throws RestException {
        // Check that the response uri is the same as the request uri
        if (response.uri() != request.uri()) {
            String message = "The remote server responded from a different uri than expected";
            throw new RestException(message);
        }

        // Check that the response status code is 200 or 201
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            String message = String.format("The remote server responded with an error code %s : %s", response.statusCode(), response.body());
            throw new RestException(message);
        }

        // Check that the answer is not empty
        String responseString = response.body();
        if (responseString == null || responseString.isBlank()) {
            String message = "The remote server responded successfully, but returned an empty body";
            throw new RestException(message);
        }
    }

    /**
     * Validate okhttp response (uri validity, response code and body presence)
     *
     * @param response
     */
    public ResponseBody validateResponse(Response response) throws RestException {

        // Verify that the response status code is 200 or 201
        if (!response.isSuccessful()) {
            String message = String.format("The remote server responded with an error code %s : %s", response.code(), response.body());
            throw new RestException(message);
        }

        return response.body();
    }

    /**
     * Serialize the object and handle serialization errors
     *
     * @param object object to serialize
     * @return serialized object
     */
    public String serialize(Object object) throws RestException {
        String serialized;
        try {
            serialized = MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            String message = String.format("Error parsing the request body: %s", e.getMessage());
            throw new RestException(message);
        }
        return serialized;
    }

    /**
     * Deserialize the http response and handle deserialization errors
     *
     * @param response  the http response to deserialize
     * @param valueType the class in which to deserialize the response
     * @return deserialized object
     */
    public <T> T deserialize(String response, Class<T> valueType) throws RestException {
        try {
            return MAPPER.readValue(response, valueType);
        } catch (Exception e) {
            String message = String.format("Error parsing the response body: %s", e.getMessage());
            throw new RestException(message);
        }
    }

}
