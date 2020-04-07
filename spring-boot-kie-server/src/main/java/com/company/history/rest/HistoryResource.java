package com.company.history.rest;

import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_INSTANCES_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_EVENTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.resources.Messages.TASK_INSTANCE_NOT_FOUND;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;

import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.api.TaskNotFoundException;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.company.history.service.CustomRuntimeDataServiceBase;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Path("/history")
public class HistoryResource {

	Logger logger = LoggerFactory.getLogger(HistoryResource.class);

	@Autowired
	@Qualifier("historyRuntimeDataServiceBase")
	CustomRuntimeDataServiceBase historyRuntimeDataServiceBase;
	@Autowired
	private KieServer server;

	@GET
	@Path("/processes/instances/{pid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHistoryPlogByPid(@Context HttpHeaders headers,
			@ApiParam(value = "process instance id to retrieve process instance", required = true) @PathParam("pid") Long pid,
			@ApiParam(value = "load process instance variables or not, defaults to false", required = false) @QueryParam("withVars") boolean withVars) {

		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);

		ProcessInstance response = historyRuntimeDataServiceBase.getProcessInstanceById(pid, withVars);
		logger.info("response response {}", response);

		return createCorrectVariant(response, headers, Response.Status.OK, conversationIdHeader);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/processes/{pdefid}/instances")

	public Response getHistoryPlogByProcessDefId(@Context HttpHeaders headers,
			@ApiParam(value = "process id to filter process instance", required = true) @PathParam("pdefid") String processId,
			@ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues = "1,2,3") @QueryParam("status") List<Integer> status,
			@ApiParam(value = "optinal process instance initiator - user who started process instance to filtr process instances", required = false) @QueryParam("initiator") String initiator,
			@ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
			@ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
			@ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
			@ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);

		ProcessInstanceList response = historyRuntimeDataServiceBase.getProcessInstancesByProcessId(processId, status,
				initiator, page, pageSize, sort, sortOrder);
		return createCorrectVariant(response, headers, Response.Status.OK, conversationIdHeader);

	}

	@ApiOperation(value = "Returns all process instances filtered by optional parameters.", response = ProcessInstanceList.class, code = 200)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
			@ApiResponse(code = 200, message = "Successfull response", examples = @Example(value = {
					@ExampleProperty(mediaType = JSON, value = GET_PROCESS_INSTANCES_RESPONSE_JSON) })) })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/processes/instances")
	public Response getProcessInstances(@Context HttpHeaders headers,
			@ApiParam(value = "optional process instance status (active, completed, aborted) - defaults ot active (1) only", required = false, allowableValues = "1,2,3") @QueryParam("status") List<Integer> status,
			@ApiParam(value = "optional process instance initiator - user who started process instance to filter process instances", required = false) @QueryParam("initiator") String initiator,
			@ApiParam(value = "optional process name to filter process instances", required = false) @QueryParam("processName") String processName,
			@ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
			@ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
			@ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
			@ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);
		ProcessInstanceList response = historyRuntimeDataServiceBase.getProcessInstances(status, initiator, processName,
				page, pageSize, sort, sortOrder);
		logger.debug("Returning result of process instance search: {}", response);

		return createCorrectVariant(response, headers, Response.Status.OK, conversationIdHeader);
	}

	@GET
	@Path("/tasks/instancesByUser")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAllAuditTaskByUser(@Context HttpHeaders headers,
			@ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String userId,
			@ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
			@ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
			@ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
			@ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

		Variant v = getVariant(headers);
		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);
		try {

			TaskSummaryList response = historyRuntimeDataServiceBase.getAllAuditTask(userId, page, pageSize, sort,
					sortOrder);

			return createCorrectVariant(response, headers, Response.Status.OK, conversationIdHeader);

		} catch (Exception e) {
			logger.error("Unexpected error during processing {}", e.getMessage(), e);
			return internalServerError(errorMessage(e), v, conversationIdHeader);
		}
	}

	@GET
	@Path("/tasks/allinstances")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getAllAuditTask(@Context HttpHeaders headers,
			@ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
			@ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
			@ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
			@ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

		Variant v = getVariant(headers);
		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);
		try {

			TaskSummaryList response = historyRuntimeDataServiceBase.getAllAuditTaskNoUser(page, pageSize, sort,
					sortOrder);

			return createCorrectVariant(response, headers, Response.Status.OK, conversationIdHeader);

		} catch (Exception e) {
			logger.error("Unexpected error during processing {}", e.getMessage(), e);
			return internalServerError(errorMessage(e), v, conversationIdHeader);
		}
	}

	@ApiOperation(value = "Returns information about a specified task instance.", response = TaskInstance.class, code = 200)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
			@ApiResponse(code = 404, message = "Task not found for given id"),
			@ApiResponse(code = 200, message = "Successfull response", examples = @Example(value = {
					@ExampleProperty(mediaType = JSON, value = GET_TASK_RESPONSE_JSON) })) })
	@GET
	@Path("/tasks/instances/{tid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaskById(@Context HttpHeaders headers,
			@ApiParam(value = "task id to load task instance", required = true) @PathParam("tid") Long taskId,
			@ApiParam(value = "optional include SLA data - defaults to false", required = false) @QueryParam("withSLA") @DefaultValue("false") boolean withSLA) {
		Variant v = getVariant(headers);
		// no container id available so only used to transfer conversation id if given
		// by client
		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);

		try {
			TaskInstance userTaskDesc = historyRuntimeDataServiceBase.getTaskById(taskId, withSLA);
			return createCorrectVariant(userTaskDesc, headers, Response.Status.OK, conversationIdHeader);
		} catch (TaskNotFoundException e) {
			return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v, conversationIdHeader);
		} catch (Exception e) {
			logger.error("Unexpected error during processing {}", e.getMessage(), e);
			return internalServerError(errorMessage(e), v, conversationIdHeader);
		}
	}

	@ApiOperation(value = "Returns events for a specified task instance.", response = TaskEventInstanceList.class, code = 200)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
			@ApiResponse(code = 200, message = "Successfull response", examples = @Example(value = {
					@ExampleProperty(mediaType = JSON, value = GET_TASK_EVENTS_RESPONSE_JSON) })) })
	@GET
	@Path("/tasks/instances/{tid}/events")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTaskEvents(@Context HttpHeaders headers,
			@ApiParam(value = "task id to load task events for", required = true) @PathParam("tid") Long taskId,
			@ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
			@ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
			@ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort,
			@ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {
		Variant v = getVariant(headers);
		// no container id available so only used to transfer conversation id if given
		// by client
		Header conversationIdHeader = buildConversationIdHeader("", context(), headers);
		try {
			TaskEventInstanceList result = historyRuntimeDataServiceBase.getTaskEvents(taskId, page, pageSize, sort,
					sortOrder);
			return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
		} catch (TaskNotFoundException e) {
			return notFound(MessageFormat.format(TASK_INSTANCE_NOT_FOUND, taskId), v, conversationIdHeader);
		} catch (Exception e) {
			logger.error("Unexpected error during processing {}", e.getMessage(), e);
			return internalServerError(errorMessage(e), v, conversationIdHeader);
		}
	}

	public KieServerRegistry context() {
		return ((KieServerImpl) server).getServerRegistry();
	}

}
