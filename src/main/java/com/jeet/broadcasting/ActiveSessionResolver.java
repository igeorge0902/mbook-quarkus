package com.jeet.broadcasting;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ActiveSessionResolver {
    @ConfigProperty(name = "active-sessions.url",
            defaultValue = "http://simple-service-webapp:8080/simple-service-webapp/webapi/myresource/admin")
    String url;

    @Inject
    ObjectMapper mapper;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2)).build();

    /** Fresh device-to-distinct-user snapshot; any malformed response fails closed. */
    public Map<String, Set<String>> deviceUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(2)).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Active sessions HTTP " + response.statusCode());
            }
            JsonNode root = mapper.readerFor(JsonNode.class)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(response.body());
            if (root == null || !root.isArray()) {
                throw new IllegalStateException("Active sessions must be a JSON array");
            }
            Map<String, Set<String>> result = new HashMap<>();
            for (JsonNode row : root) {
                if (!row.isObject()) {
                    throw new IllegalStateException("Active session must be a JSON object");
                }
                JsonNode device = row.get("deviceId");
                JsonNode user = row.get("user");
                if ((device != null && !device.isTextual()) || (user != null && !user.isTextual())) {
                    throw new IllegalStateException("Active session identity must be textual");
                }
                if (device == null || user == null || device.textValue().isBlank() || user.textValue().isBlank()) {
                    continue;
                }
                result.computeIfAbsent(device.textValue(), ignored -> new HashSet<>()).add(user.textValue());
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Active sessions lookup interrupted", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Active sessions lookup failed", e);
        }
    }
}
