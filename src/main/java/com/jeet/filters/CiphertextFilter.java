package com.jeet.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class CiphertextFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        if (path != null
                && (path.matches("^/?user/[^/]+/[^/]+$") || path.matches("^/?device/[^/]+$"))) {

            String headerToken = requestContext.getHeaderString("Ciphertext");
            String token2 = requestContext.getHeaderString("token2");

            if (headerToken == null || token2 == null || !headerToken.trim().equals(token2.trim())) {
                requestContext.abortWith(
                        Response.status(Response.Status.FORBIDDEN)
                                .entity("Cannot access")
                                .build()
                );
            }
        }
    }
}
