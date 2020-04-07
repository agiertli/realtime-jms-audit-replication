package com.company.history.jms.producers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.event.AuditEvent;
import org.jbpm.process.audit.event.DefaultAuditEventBuilderImpl;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.internal.identity.IdentityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomAuditEventBuilder extends DefaultAuditEventBuilderImpl {

	@Autowired
	private IdentityProvider identityProvider;

	private final Boolean allowSetInitiator = Boolean
			.parseBoolean(System.getProperty("org.kie.server.bypass.auth.user", "false"));

	public AuditEvent buildEvent(ProcessStartedEvent pse, String deploymentUnitId) {

		ProcessInstanceLog log = (ProcessInstanceLog) super.buildEvent(pse);
		log.setIdentity(getIdentity(pse));
		log.setExternalId(deploymentUnitId);
		return log;
	}

	public AuditEvent buildEvent(ProcessCompletedEvent pce, Object log, String deploymentUnitId) {
		ProcessInstanceLog instanceLog = (ProcessInstanceLog) super.buildEvent(pce, log);
		instanceLog.setExternalId(deploymentUnitId);
		return instanceLog;

	}

	public AuditEvent buildEvent(ProcessNodeTriggeredEvent pnte, String deploymentUnitId) {
		NodeInstanceLog nodeInstanceLog = (NodeInstanceLog) super.buildEvent(pnte);
		nodeInstanceLog.setExternalId(deploymentUnitId);
		return nodeInstanceLog;

	}

	public AuditEvent buildEvent(ProcessNodeTriggeredEvent pnte, Object log, String deploymentUnitId) {
		NodeInstanceLog nodeInstanceLog = (NodeInstanceLog) super.buildEvent(pnte, log);
		nodeInstanceLog.setExternalId(deploymentUnitId);
		return nodeInstanceLog;

	}

	public AuditEvent buildEvent(ProcessNodeLeftEvent pnle, Object log, String deploymentUnitId) {
		NodeInstanceLog nodeInstanceLog = (NodeInstanceLog) super.buildEvent(pnle, log);
		nodeInstanceLog.setExternalId(deploymentUnitId);
		return nodeInstanceLog;
	}

	public AuditEvent buildEvent(ProcessVariableChangedEvent pvce, String deploymentUnitId) {
		VariableInstanceLog variableLog = (VariableInstanceLog) super.buildEvent(pvce);
		variableLog.setExternalId(deploymentUnitId);
		return variableLog;
	}

	/**
	 * Utilitary method to get the identity to save on ProcessInstanceLog. It checks
	 * if bypass user authentication is set and if set, checks if the value of
	 * initiator in process variables is set and uses it. Otherwise will use the
	 * value from the identity provider.
	 *
	 * @param ProcessStartedEvent pse
	 *
	 * @return String the identity to be used as process starter
	 */
	private String getIdentity(ProcessStartedEvent pse) {
		String identity = identityProvider.getName();
		if (allowSetInitiator) {
			ProcessInstance pi = (ProcessInstance) pse.getProcessInstance();
			VariableScopeInstance variableScope = (VariableScopeInstance) pi
					.getContextInstance(VariableScope.VARIABLE_SCOPE);
			Map<String, Object> processVariables = variableScope.getVariables();
			String initiator = (String) processVariables.get("initiator");

			identity = !StringUtils.isEmpty(initiator) ? initiator : identity;
		}
		return identity;
	}

}
