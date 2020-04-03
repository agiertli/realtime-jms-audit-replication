package com.company.history.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Path("/history")
public class HistoryResource {

	Logger logger = LoggerFactory.getLogger(HistoryResource.class);

	@Autowired
	@Qualifier("historyRuntimeDataServiceBase")
	RuntimeDataServiceBase historyRuntimeDataServiceBase;

	@GET
	@Path("/processes/instances/{pid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHistoryPlog(@PathParam("pid") Long pid) {

		ProcessInstance response = historyRuntimeDataServiceBase.getProcessInstanceById(pid);
		logger.info("response response {}", response);

		return Response.ok().entity(response).build();
	}

}
