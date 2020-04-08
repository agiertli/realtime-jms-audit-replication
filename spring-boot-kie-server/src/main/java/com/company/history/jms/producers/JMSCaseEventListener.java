package com.company.history.jms.producers;

import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import javax.jms.TextMessage;

import org.jbpm.casemgmt.impl.jms.AsyncCaseInstanceAuditEventProducer;
import org.jbpm.casemgmt.impl.model.AuditCaseInstanceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

@Component
public class JMSCaseEventListener extends AsyncCaseInstanceAuditEventProducer {
	Logger logger = LoggerFactory.getLogger(AsyncCaseInstanceAuditEventProducer.class);

	@Autowired
	private JmsTemplate jmsTemplate;
	private XStream xstream;

	@Value("${audit.queue}")
	private String queue;

	public JMSCaseEventListener() {
		super();
		initXStream();

	}

	@Override
	protected void sendMessage(Integer eventType, AuditCaseInstanceData eventData, int priority) {
		String eventXml = xstream.toXML(eventData);

		logger.info("Case XML Event: \n {}", eventXml);

		jmsTemplate.send(queue, messageCreator -> {
			TextMessage message = messageCreator.createTextMessage(eventXml);
			message.setIntProperty("EventType", eventType);
			message.setStringProperty("LogType", "Case");
			message.setJMSPriority(priority);
			logger.info("Case Sending JMS Message {}", message);
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
