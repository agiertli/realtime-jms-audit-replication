package com.company.history.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.shared.services.impl.commands.QueryNameCommand;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import static org.kie.server.services.jbpm.ConvertUtils.*;

public class CustomRuntimeDataServiceBase extends RuntimeDataServiceBase {

	CustomRuntimeDataServiceImpl runtimeDataService;

	public CustomRuntimeDataServiceBase(CustomRuntimeDataServiceImpl delegate, KieServerRegistry context) {
		super(delegate, context);
		this.runtimeDataService = delegate;
	}

	public ProcessInstanceList getProcessInstancesByDate(List<Integer> status, String startFrom, String startTo,
			String initiator, String processName, Integer page, Integer pageSize, String sort, boolean sortOrder) {

		if (sort == null || sort.isEmpty()) {
			sort = "ProcessInstanceId";
		}
		if (status == null || status.isEmpty()) {
			status = new ArrayList<Integer>();
			status.add(ProcessInstance.STATE_ACTIVE);
		}
		Collection<ProcessInstanceDesc> instances = null;

		instances = runtimeDataService.getProcessInstancesByDate(status, startFrom, startTo, initiator,
				buildQueryContext(page, pageSize, sort, sortOrder));

		ProcessInstanceList processInstanceList = convertToProcessInstanceList(instances);
		logger.debug("Returning result of process instance search: {}", processInstanceList);

		return processInstanceList;

	}

	public TaskSummaryList getAllAuditTaskNoUser(Integer page, Integer pageSize, String sort, boolean sortOrder) {

		logger.debug("About to search for tasks available");
		List<AuditTask> tasks = runtimeDataService
				.getAllAuditTaskNoUser(buildQueryFilter(page, pageSize, sort, sortOrder));

		logger.debug("Found {} tasks available ", tasks.size());
		TaskSummaryList result = null;
		if (tasks == null) {
			result = new TaskSummaryList(new org.kie.server.api.model.instance.TaskSummary[0]);
		} else {
			org.kie.server.api.model.instance.TaskSummary[] instances = new org.kie.server.api.model.instance.TaskSummary[tasks
					.size()];
			int counter = 0;
			for (AuditTask taskSummary : tasks) {

				org.kie.server.api.model.instance.TaskSummary task = org.kie.server.api.model.instance.TaskSummary
						.builder().id(taskSummary.getTaskId()).name(taskSummary.getName())
						.description(taskSummary.getDescription()).taskParentId(taskSummary.getParentId())
						.activationTime(taskSummary.getActivationTime()).actualOwner(taskSummary.getActualOwner())
						.containerId(taskSummary.getDeploymentId()).createdBy(taskSummary.getCreatedBy())
						.createdOn(taskSummary.getCreatedOn()).expirationTime(taskSummary.getDueDate())
						.priority(taskSummary.getPriority()).processId(taskSummary.getProcessId())
						.processInstanceId(taskSummary.getProcessInstanceId()).status(taskSummary.getStatus()).build();
				instances[counter] = task;
				counter++;
			}
			result = new TaskSummaryList(instances);
		}

		return result;
	}

	public CustomRuntimeDataServiceImpl getRuntimeDataService() {
		return runtimeDataService;
	}

	public void setRuntimeDataService(CustomRuntimeDataServiceImpl runtimeDataService) {
		this.runtimeDataService = runtimeDataService;
	}

}
