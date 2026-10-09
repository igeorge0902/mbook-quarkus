package com.jeet.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeet.rest.responserecord.news.ErrorResponse;
import com.jeet.service.NewsSettingsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/rest/news-settings/screen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NewsSettingsController {
    @Inject
    NewsSettingsService settings;

    @Inject
    ApiResponseFactory responses;

    @Inject
    ObjectMapper mapper;

    @GET
    public Response get(@HeaderParam("user") String user) {
        return respond(settings.get(user));
    }

    @PUT
    public Response put(@HeaderParam("user") String user, String body) {
        JsonNode json;
        if (body == null || body.isBlank()) {
            return invalidBody();
        }
        try {
            json = mapper.readerFor(JsonNode.class)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(body);
        } catch (JsonProcessingException e) {
            return invalidBody();
        }
        if (json == null || !json.isObject() || !json.path("enabled").isBoolean()) {
            return invalidBody();
        }
        // Ownership fields in the body are intentionally ignored.
        return respond(settings.put(user, json.get("enabled").booleanValue()));
    }

    private Response invalidBody() {
        return responses.error(Response.Status.BAD_REQUEST, new ErrorResponse("invalid_body"));
    }

    private Response respond(NewsSettingsService.Result result) {
        return responses.builder(result.status(), result.body()).build();
    }
}
