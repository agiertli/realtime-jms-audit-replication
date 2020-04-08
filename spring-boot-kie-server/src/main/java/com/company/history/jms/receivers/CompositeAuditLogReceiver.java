package com.company.history.jms.receivers;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CompositeAuditLogReceiver {

	private static final Logger logger = LoggerFactory.getLogger(CompositeAuditLogReceiver.class);

	@Autowired
	private ProcessAuditReceiver processLogsReceiver;

	@Autowired
	private TaskAuditReceiver taskLogReceiver;

	@Autowired
	private CaseAuditReceiver caseAuditReceiver;

	@JmsListener(destination = "${audit.queue}")
	public void receiveMessage(Message message) {

		logger.debug("Audit log message received {}", message);
		try {
			String logType = message.getStringProperty("LogType");
			logger.debug("LogType property on message set to {}", logType);

			if ("Process".equals(logType)) {
				processLogsReceiver.onMessage(message);
			} else if ("Task".equals(logType)) {
				taskLogReceiver.onMessage(message);
			} else if ("Case".equals(logType) && caseAuditReceiver != null) {

				logger.info("Case Audit received");
				caseAuditReceiver.onMessage(message);
			} else {
				logger.warn("Unexpected message {} with log type {}, consuming and ignoring", message, logType);
			}

		} catch (JMSException e) {
			logger.error("Unexpected JMS exception while processing audit log message", e);
		}

	}
}
