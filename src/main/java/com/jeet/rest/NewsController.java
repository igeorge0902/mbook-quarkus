package com.jeet.rest;

import com.jeet.broadcasting.eventModel.EventState;
import com.jeet.rest.responserecord.news.InfoResponse;
import com.jeet.service.NewsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@RequestScoped
@Path("/rest")
public class NewsController {

    @Inject
    NewsService newsService;

    @Inject
    ApiResponseFactory responseFactory;

    @GET
    @Path("/news/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewEventsForUser(@PathParam("user") String user) {
        List<EventState> events = newsService.getNewEventsForUser(user);

        if (events == null || events.isEmpty()) {
            return responseFactory.ok(new InfoResponse("No new event"));
        }
        return responseFactory.ok(events);
    }
}
