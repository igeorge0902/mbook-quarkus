package com.jeet.rest;

import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.broadcasting.eventModel.EventState;
import com.jeet.service.BookingHandlerImpl;
import com.jeet.utils.AesUtil;
import com.jeet.utils.CustomNotFoundException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;

import jakarta.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

@RequestScoped
@Path("/rest")
public class BookController implements Serializable {

	private static final String SALT = "3FF2EC019C627B945225DEBAD71A01B6985FE84C95A70EB132882F88C0A59A55";
	private static final String IV = "F27D5C9927726BCEFE7510B1BDD3D137";
	private static final String ORIGINPLAINTEXT = "G";
	private static final String PASSPHRASE = "SecretPassphrase";
	private static final int KEYSIZE = 128;
	private static final int ITERATIONCOUNT = 1000;
	private static final AesUtil aesUtil = new AesUtil(KEYSIZE, ITERATIONCOUNT);

	@Inject
	private BookingHandlerImpl bookingHandler;

	@GET
	@Path("/device/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDevice(@PathParam("uuid") String uuid) {
		List<Devices> devices = bookingHandler.getDevice(uuid);

		if (devices == null || devices.isEmpty() || devices.get(0).getId() == 0) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\":\"Device not found\"}")
					.build();
		}

		GenericEntity<List<Devices>> entity = new GenericEntity<>(devices) {};
		return Response.ok(entity)
				.header("Device", devices.get(0).getDevice())
				.build();
	}

	@GET
	@Path("/user/{user}/{token1}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@Context HttpServletRequest request,
							@Context HttpHeaders headers,
							@PathParam("user") String user) throws ParseException {

		String token2 = request.getHeader("X-Token");

		JSONObject userJson = new JSONObject();
		Object errorDetails = request.getAttribute("Error Details");

		if (errorDetails != null) {
			userJson.put("Error Details", new JSONObject(errorDetails.toString()));
		}

		Logins login = bookingHandler.getUser(user);

		userJson.put("uuid", login.getUuid());
		userJson.put("user", login.getUser());
		userJson.put("email", login.getEmail());
		userJson.put("profilePicture", login.getProfilePicture());

		if (login.getId() == 0) {
			return Response.status(Response.Status.PRECONDITION_FAILED)
					.entity(login)
					.build();
		}

		Response.ResponseBuilder responseBuilder = Response.ok(userJson.toString());

		if (errorDetails != null) {
			responseBuilder.status(300)
					.header("User", login.getUuid())
					.header("X-Token", token2);
		} else {
			responseBuilder.status(Response.Status.OK)
					.header("User", login.getUuid());
		}

		return responseBuilder.build();
	}

	@GET
	@Path("/news/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewEventsForUser(@PathParam("user") String user) {
		List<EventState> events = bookingHandler.getNewEventsForUser(user);

		if (events == null || events.isEmpty()) {
			JSONObject json = new JSONObject();
			json.put("Info", "No new event");
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		}

		return Response.ok(events, MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/newuser/{newuser}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewUser(@PathParam("newuser") String newuser) {
		int count = bookingHandler.getNewUser(newuser);

		JSONObject json = new JSONObject();
		json.put("name", "My name is...");

		if (count > 0) {
			return Response.status(Response.Status.PRECONDITION_FAILED)
					.entity(json.toString())
					.type(MediaType.APPLICATION_JSON)
					.build();
		}

		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/newemail/{newemail}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewEmail(@PathParam("newemail") String newemail) {
		int count = bookingHandler.getNewEmail(newemail);

		JSONObject json = new JSONObject();
		json.put("name", "My email is...");

		if (count > 0) {
			return Response.status(Response.Status.PRECONDITION_FAILED)
					.entity(json.toString())
					.type(MediaType.APPLICATION_JSON)
					.build();
		}

		return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/images/{image}")
	@Produces("image/*")
	public Response getImage(@PathParam("image") String image) throws IOException {
		File file = new File("/Users/georgegaspar/Pictures/Exports/" + image);

		if (!file.exists()) {
			throw new CustomNotFoundException();
		}

		BufferedImage img = ImageIO.read(file);
		String mimeType = new MimetypesFileTypeMap().getContentType(file);

		return Response.ok(img, mimeType).build();
	}
}