package com.jeet.rest.requestbean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;

public class UserParams {

    @Context
    private HttpServletRequest request;

    @PathParam("user")
    private String user;

    @PathParam("token1")
    private String token1;

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getUser() {
        return user;
    }

    public String getToken1() {
        return token1;
    }
}
