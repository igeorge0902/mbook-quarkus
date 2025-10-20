package com.jeet.utils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
	public class EntityNotFoundMapper implements ExceptionMapper<EntityNotFoundException> {
	 
	public Response toResponse(EntityNotFoundException ex) {
	
		return Response.status(404).entity(ex.getMessage()).type("text/plain").build();
		
	  }
	}
	

