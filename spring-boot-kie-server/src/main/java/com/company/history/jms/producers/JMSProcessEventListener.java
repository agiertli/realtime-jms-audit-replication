package com.company.history.jms.producers;

import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import java.util.List;

import javax.jms.TextMessage;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.process.audit.variable.ProcessIndexerManager;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

/**
 * Listens to jBPM Process Events and forwards them to JMS Queue Almost exact
 * copy of
 * https://github.com/kiegroup/jbpm/blob/master/jbpm-audit/src/main/java/org/jbpm/process/audit/jms/AsyncAuditLogProducer.java
 */
@Component
@Profile("!local-case")
public class JMSProcessEventListener implements ProcessEventListener {

	Logger logger = LoggerFactory.getLogger(JMSProcessEventListener.class);

	public static final int BEFORE_START_EVENT_TYPE = 0;
	public static final int AFTER_START_EVENT_TYPE = 1;
	public static final int BEFORE_COMPLETE_EVENT_TYPE = 2;
	public static final int AFTER_COMPLETE_EVENT_TYPE = 3;
	public static final int BEFORE_NODE_ENTER_EVENT_TYPE = 4;
	public static final int AFTER_NODE_ENTER_EVENT_TYPE = 5;
	public static final int BEFORE_NODE_LEFT_EVENT_TYPE = 6;
	public static final int AFTER_NODE_LEFT_EVENT_TYPE = 7;
	public static final int BEFORE_VAR_CHANGE_EVENT_TYPE = 8;
	public static final int AFTER_VAR_CHANGE_EVENT_TYPE = 9;

	@Autowired
	private CustomAuditEventBuilder builder;
	private ProcessIndexerManager indexManager = ProcessIndexerManager.get();
	private XStream xstream;

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Value("${audit.queue}")
	private String queue;

	@Autowired
	private RuntimeDataService runtimeDataService;

	public JMSProcessEventListener() {
		initXStream();

	}

	private void initXStream() {
		if (xstream == null) {
			xstream = createTrustingXStream();
			String[] voidDeny = { "void.class", "Void.class" };
			xstream.denyTypes(voidDeny);
		}
	}

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {
		NodeInstanceLog log = (NodeInstanceLog) builder.buildEvent(event, null,
				getProcessInstanceDeploymentId(event.getProcessInstance().getId()));
		sendMessage(log, AFTER_NODE_LEFT_EVENT_TYPE, 1);
	}

	@Override
	public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
		// trigger this to record some of the data (like work item id) after activity
		// was triggered
		NodeInstanceLog log = (NodeInstanceLog) ((NodeInstanceImpl) event.getNodeInstance()).getMetaData()
				.get("NodeInstanceLog");
		NodeInstanceLog logUpdated = (NodeInstanceLog) builder.buildEvent(event, log,
				getProcessInstanceDeploymentId(event.getProcessInstance().getId()));
		if (logUpdated != null) {
			sendMessage(log, AFTER_NODE_ENTER_EVENT_TYPE, 2);
		}
	}

	@Override
	public void afterProcessCompleted(ProcessCompletedEvent event) {
		ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(event, null,
				getProcessInstanceDeploymentId(event.getProcessInstance().getId()));
		sendMessage(log, AFTER_COMPLETE_EVENT_TYPE, 0);
	}

	@Override
	public void afterProcessStarted(ProcessStartedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterVariableChanged(ProcessVariableChangedEvent event) {
		List<org.kie.api.runtime.manager.audit.VariableInstanceLog> variables = indexManager.index(getBuilder(), event);
		for (org.kie.api.runtime.manager.audit.VariableInstanceLog log : variables) {
			((org.jbpm.process.audit.VariableInstanceLog) log)
					.setExternalId(getProcessInstanceDeploymentId(log.getProcessInstanceId()));
			sendMessage(log, AFTER_VAR_CHANGE_EVENT_TYPE, 1);
		}
	}

	@Override
	public void beforeNodeLeft(ProcessNodeLeftEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
		NodeInstanceLog log = (NodeInstanceLog) builder.buildEvent(event, getProcessInstanceDeploymentId(event.getProcessInstance().getId()));
		sendMessage(log, BEFORE_NODE_ENTER_EVENT_TYPE, 8);
		((NodeInstanceImpl) event.getNodeInstance()).getMetaData().put("NodeInstanceLog", log);
	}

	private void sendMessage(Object messageContent, Integer eventType, int priority) {

		String eventXml = xstream.toXML(messageContent);

		logger.info("Process XML Event: \n {}", eventXml);

		jmsTemplate.send(queue, messageCreator -> {
			TextMessage message = messageCreator.createTextMessage(eventXml);
			message.setIntProperty("EventType", eventType);
			message.setStringProperty("LogType", "Process");
			message.setJMSPriority(priority);
			logger.info("Sending JMS Message {}", message);
			return message;
		});

	}

	@Override
	public void beforeProcessCompleted(ProcessCompletedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeProcessStarted(ProcessStartedEvent event) {
		ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(event,
				getProcessInstanceDeploymentId(event.getProcessInstance().getId()));
		sendMessage(log, BEFORE_START_EVENT_TYPE, 9);
	}

	@Override
	public void beforeVariableChanged(ProcessVariableChangedEvent arg0) {
		// TODO Auto-generated method stub

	}

	public AuditEventBuilder getBuilder() {
		return builder;
	}

	private String getProcessInstanceDeploymentId(Long pid) {
		return runtimeDataService.getProcessInstanceById(pid).getDeploymentId();
	}

}