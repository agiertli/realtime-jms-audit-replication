package com.company.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Path("/history")
public class HistoryProxy {

	Logger logger = LoggerFactory.getLogger(HistoryProxy.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${kieserver.history.url}")
	private String HISTORY_URL;

	@GET
	@Path("/plog/{pid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHistoryPlog(@PathParam("pid") Long pid) {

		logger.info("custom endpoint invoked");

		logger.info("Returning plog from {}", HISTORY_URL);

		ResponseEntity<String> response = restTemplate.getForEntity(HISTORY_URL + "/history/plog/" + pid, String.class);

		return Response.ok().entity(response.getBody()).build();
	}

}
