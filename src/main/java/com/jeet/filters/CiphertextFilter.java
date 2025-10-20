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
		System.out.println("Ciphertext filter...");
		System.out.println("Uri: " + requestContext.getUriInfo().getBaseUri());
		System.out.println("Absolute Path: " + requestContext.getUriInfo().getAbsolutePath());
		System.out.println("Path: " + requestContext.getUriInfo().getPath());

		if (requestContext.getUriInfo().getPath().contains("user")) {
			System.out.println("Ciphertext filter running...");

			String headerToken = requestContext.getHeaderString("Ciphertext");
			String token2 = requestContext.getHeaderString("token2");

			System.out.println("headerToken: " + headerToken);
			System.out.println("token2: " + token2);

			if (headerToken == null || token2 == null ||
					!headerToken.trim().equals(token2.trim())) {
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
						.entity("Cannot access")
						.build());
			}
		}
	}
}