package com.jeet.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ApiResponseFactory {

    public Response ok(Object body) {
        return response(Response.Status.OK, body);
    }

    public Response ok(Object body, Response.Status status) {
        return response(status, body);
    }

    public Response error(Response.Status status, Object body) {
        return response(status, body);
    }

    public Response.ResponseBuilder builder(int status, Object body) {
        return json(Response.status(status), body);
    }

    private Response response(Response.Status status, Object body) {
        return json(Response.status(status), body).build();
    }

    private Response.ResponseBuilder json(Response.ResponseBuilder builder, Object body) {
        if (body != null) {
            builder.entity(body);
        }
        return builder.type(MediaType.APPLICATION_JSON);
    }
}
