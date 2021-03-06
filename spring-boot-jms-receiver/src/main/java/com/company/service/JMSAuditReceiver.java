package com.company.service;

import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jbpm.process.audit.AbstractAuditLogger;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtworks.xstream.XStream;

@Component
@Transactional
public class JMSAuditReceiver {

	Logger logger = LoggerFactory.getLogger(JMSAuditReceiver.class);
	private XStream xstream;

	@PersistenceContext
	EntityManager em;

	public JMSAuditReceiver() {
		initXStream();

	}

	private void initXStream() {
		if (xstream == null) {
			xstream = createTrustingXStream();
			String[] voidDeny = { "void.class", "Void.class" };
			xstream.denyTypes(voidDeny);
		}
	}

	@JmsListener(destination = "audit-queue")
	public void receiveMessage(Object message) {

		if (message instanceof TextMessage) {

			TextMessage textMessage = (TextMessage) message;
			logger.info("Message received : {}", textMessage);
			try {
				String messageContent = textMessage.getText();
				Integer eventType = textMessage.getIntProperty("EventType");
				Object event = xstream.fromXML(messageContent);

				switch (eventType) {
				case AbstractAuditLogger.AFTER_NODE_ENTER_EVENT_TYPE:
					NodeInstanceLog nodeAfterEnterEvent = (NodeInstanceLog) event;
					if (nodeAfterEnterEvent.getWorkItemId() != null) {
						List<NodeInstanceLog> result = em.createQuery(
								"from NodeInstanceLog as log where log.nodeInstanceId = :nodeId and log.type = 0")
								.setParameter("nodeId", nodeAfterEnterEvent.getNodeInstanceId()).getResultList();

						if (result != null && result.size() != 0) {
							NodeInstanceLog log = result.get(result.size() - 1);
							log.setWorkItemId(nodeAfterEnterEvent.getWorkItemId());

							em.merge(log);
						}
					}
					break;

				case AbstractAuditLogger.AFTER_COMPLETE_EVENT_TYPE:
					ProcessInstanceLog processCompletedEvent = (ProcessInstanceLog) event;
					List<ProcessInstanceLog> result = em.createQuery(
							"from ProcessInstanceLog as log where log.processInstanceId = :piId and log.end is null")
							.setParameter("piId", processCompletedEvent.getProcessInstanceId()).getResultList();

					if (result != null && result.size() != 0) {
						ProcessInstanceLog log = result.get(result.size() - 1);
						log.setOutcome(processCompletedEvent.getOutcome());
						log.setStatus(processCompletedEvent.getStatus());
						log.setEnd(processCompletedEvent.getEnd());
						log.setDuration(processCompletedEvent.getDuration());
						em.merge(log);
					}
					break;
				default:
					em.persist(event);
					break;
				}
				em.flush();
				em.close();
			} catch (JMSException e) {
				e.printStackTrace();
				throw new RuntimeException("Exception when receiving audit event event", e);
			}
		}

	}

}