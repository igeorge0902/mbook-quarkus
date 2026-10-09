package com.jeet.rest;

import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.rest.requestbean.UserParams;
import com.jeet.rest.responserecord.account.NameResponse;
import com.jeet.rest.responserecord.account.UserResponse;
import com.jeet.service.AccountService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@RequestScoped
@Path("/rest")
public class AccountController {

    @Inject
    AccountService accountService;

    @Inject
    ApiResponseFactory responseFactory;

    @GET
    @Path("/device/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("uuid") String uuid) {
        List<Devices> devices = accountService.getDevices(uuid);

        if (devices == null || devices.isEmpty() || devices.get(0).getId() == 0) {
            return responseFactory.error(
                    Response.Status.NOT_FOUND,
                    Map.of("error", "Device not found"));
        }

        GenericEntity<List<Devices>> entity = new GenericEntity<>(devices) {
        };
        return responseFactory.builder(Response.Status.OK.getStatusCode(), entity)
                .header("Device", devices.get(0).getDevice())
                .build();
    }

    @GET
    @Path("/user/{user}/{token1}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@BeanParam UserParams params) {
        Object errorDetails = params.getRequest().getAttribute("Error Details");
        var parsedErrorDetails = accountService.parseErrorDetails(errorDetails);
        Logins login = accountService.getUser(params.getUser());

        if (login.getId() == 0) {
            return responseFactory.builder(
                    Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    login).build();
        }

        UserResponse userResponse = accountService.toUserResponse(login, parsedErrorDetails);
        Response.ResponseBuilder builder;
        if (errorDetails != null) {
            builder = responseFactory.builder(300, userResponse)
                    .header("User", login.getUuid());
            String token = params.getRequest().getHeader("X-Token");
            if (token != null) {
                builder.header("X-Token", token);
            }
        } else {
            builder = responseFactory.builder(Response.Status.OK.getStatusCode(), userResponse)
                    .header("User", login.getUuid());
        }
        return builder.build();
    }

    @GET
    @Path("/newuser/{newuser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewUser(@PathParam("newuser") String newuser) {
        int count = accountService.countUsersLike(newuser);
        NameResponse response = new NameResponse("My name is...");

        if (count > 0) {
            return responseFactory.builder(
                    Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    response).build();
        }
        return responseFactory.ok(response);
    }

    @GET
    @Path("/newemail/{newemail}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewEmail(@PathParam("newemail") String newemail) {
        int count = accountService.countEmailsLike(newemail);
        NameResponse response = new NameResponse("My email is...");

        if (count > 0) {
            return responseFactory.builder(
                    Response.Status.PRECONDITION_FAILED.getStatusCode(),
                    response).build();
        }
        return responseFactory.ok(response);
    }
}
