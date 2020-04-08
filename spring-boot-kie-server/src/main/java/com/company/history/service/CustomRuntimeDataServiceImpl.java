package com.company.history.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.shared.services.impl.commands.QueryNameCommand;
import org.kie.api.runtime.query.QueryContext;
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
	
    public Collection<ProcessInstanceDesc> getProcessInstancesByDate(List<Integer> states, String startFrom, String startTo,String initiator, QueryContext queryContext) {

        List<ProcessInstanceDesc> processInstances = null;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("states", states);
        params.put("startFrom", startFrom);
        params.put("startTo", startTo);

        applyQueryContext(params, queryContext);
        applyDeploymentFilter(params);
        if (initiator == null) {

            processInstances = commandService.execute(
    				new QueryNameCommand<List<ProcessInstanceDesc>>("getProcessInstanceByIdByDate", params));
        } 
        return Collections.unmodifiableCollection(processInstances);
    }

	public TransactionalCommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(TransactionalCommandService commandService) {
		super.setCommandService(commandService);
		this.commandService = commandService;
	}

}
