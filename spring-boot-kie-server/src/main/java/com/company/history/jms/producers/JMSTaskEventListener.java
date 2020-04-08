package com.company.history.jms.producers;

import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import javax.jms.TextMessage;

import org.jbpm.services.task.audit.impl.model.AuditTaskData;
import org.jbpm.services.task.audit.jms.AsyncTaskLifeCycleEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

@Component
public class JMSTaskEventListener extends AsyncTaskLifeCycleEventProducer {

	Logger logger = LoggerFactory.getLogger(JMSTaskEventListener.class);
	@Autowired
	private JmsTemplate jmsTemplate;
	private XStream xstream;

	@Value("${audit.queue}")
	private String queue;

	public JMSTaskEventListener() {
		super();
		initXStream();

	}

	@Override
	protected void sendMessage(AuditTaskData auditTaskData, int priority) {

		String eventXml = xstream.toXML(auditTaskData);

		logger.info("Sending Task Audit message \n{}", eventXml);

		jmsTemplate.send(queue, messageCreator -> {
			TextMessage message = messageCreator.createTextMessage(eventXml);
			message.setStringProperty("LogType", "Task");
			message.setJMSPriority(priority);
			logger.info("Sending JMS Message {}", message);
			return message;
		});

	}

	private void initXStream() {
		if (xstream == null) {
			xstream = createTrustingXStream();
			String[] voidDeny = { "void.class", "Void.class" };
			xstream.denyTypes(voidDeny);
		}
	}

}
