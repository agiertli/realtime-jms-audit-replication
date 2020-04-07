package com.company.history.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Path("/history")
@Api(value = "Custom History API", produces = MediaType.APPLICATION_JSON)
public class HistoryResource {

	Logger logger = LoggerFactory.getLogger(HistoryResource.class);

	@GET
	@Path("/example")
	public Response example() {

		String response = " {\"exampleResponse\" : \"exampleValue\" }";
		return Response.ok(response).build();

	}

}
