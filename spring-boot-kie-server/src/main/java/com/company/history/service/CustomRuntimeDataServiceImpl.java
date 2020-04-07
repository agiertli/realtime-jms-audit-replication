package com.company.history.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.QueryNameCommand;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;

public class CustomRuntimeDataServiceImpl extends RuntimeDataServiceImpl {

	private TransactionalCommandService commandService;

	public List<AuditTask> getAllAuditTaskNoUser(QueryFilter filter) {
		Map<String, Object> params = new HashMap<String, Object>();
		applyQueryContext(params, filter);
		applyQueryFilter(params, filter);
		List<AuditTask> auditTasks = commandService
				.execute(new QueryNameCommand<List<AuditTask>>("getAllAuditTasks", params));
		return auditTasks;
	}

	public TransactionalCommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(TransactionalCommandService commandService) {
		super.setCommandService(commandService);
		this.commandService = commandService;
	}

}
