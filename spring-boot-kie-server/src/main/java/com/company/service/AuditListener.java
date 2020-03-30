package com.company.service;

import com.thoughtworks.xstream.XStream;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.process.audit.event.DefaultAuditEventBuilderImpl;
import org.jbpm.process.audit.variable.ProcessIndexerManager;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import java.util.List;

import javax.jms.TextMessage;

/**
 * AuditListener
 */
@Component
public class AuditListener implements ProcessEventListener {

    Logger logger = LoggerFactory.getLogger(AuditListener.class);

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
    private AuditEventBuilder builder = new DefaultAuditEventBuilderImpl();
    private ProcessIndexerManager indexManager = ProcessIndexerManager.get();
    private XStream xstream;

    @Autowired
    private JmsTemplate jmsTemplate;

    public AuditListener() {
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
        NodeInstanceLog log = (NodeInstanceLog) builder.buildEvent(event, null);
        sendMessage(log, AFTER_NODE_LEFT_EVENT_TYPE, 1);
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        // trigger this to record some of the data (like work item id) after activity
        // was triggered
        NodeInstanceLog log = (NodeInstanceLog) ((NodeInstanceImpl) event.getNodeInstance()).getMetaData()
                .get("NodeInstanceLog");
        NodeInstanceLog logUpdated = (NodeInstanceLog) builder.buildEvent(event, log);
        if (logUpdated != null) {
            sendMessage(log, AFTER_NODE_ENTER_EVENT_TYPE, 2);
        }
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(event, null);
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
            sendMessage(log, AFTER_VAR_CHANGE_EVENT_TYPE, 1);
        }
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        NodeInstanceLog log = (NodeInstanceLog) builder.buildEvent(event);
        sendMessage(log, BEFORE_NODE_ENTER_EVENT_TYPE, 8);
        ((NodeInstanceImpl) event.getNodeInstance()).getMetaData().put("NodeInstanceLog", log);
    }

    private void sendMessage(Object messageContent, Integer eventType, int priority) {

        String eventXml = xstream.toXML(messageContent);
        
        logger.info("XML Event: \n {}",eventXml);
        
        jmsTemplate.send("audit-queue", messageCreator -> {
            TextMessage message = messageCreator.createTextMessage(eventXml);
            message.setIntProperty("EventType", eventType);
            message.setStringProperty("LogType", "Process");
            message.setJMSPriority(priority);
            logger.info("Sending JMS Message {} {} {}", message);
            return message;
        });

    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(event);
        sendMessage(log, BEFORE_START_EVENT_TYPE, 9);
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent arg0) {
        // TODO Auto-generated method stub

    }

    public AuditEventBuilder getBuilder() {
        return builder;
    }

}