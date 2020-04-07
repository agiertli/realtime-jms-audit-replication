package com.company.history.jms.receivers;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.jbpm.services.task.audit.impl.model.AuditTaskData;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.jbpm.services.task.audit.impl.model.TaskVariableImpl;
import org.kie.internal.task.api.TaskVariable.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;
import com.thoughtworks.xstream.XStream;

@Component
@Transactional
public class TaskAuditReceiver {

	Logger logger = LoggerFactory.getLogger(TaskAuditReceiver.class);

	private XStream xstream;

	public TaskAuditReceiver() {
		initXStream();

	}

	private void initXStream() {
		if (xstream == null) {
			xstream = createTrustingXStream();
			String[] voidDeny = { "void.class", "Void.class" };
			xstream.denyTypes(voidDeny);
		}
	}

	@Autowired
	@Qualifier("auditEntityManager")
	EntityManagerFactory emf;

	public void onMessage(Object message) {
		EntityManager em = this.emf.createEntityManager();
		TextMessage textMessage = (TextMessage) message;
		logger.info("[Task Audit Log] Message received : {}", textMessage);

		try {
			String messageContent = textMessage.getText();

			AuditTaskData auditTaskData = (AuditTaskData) xstream.fromXML(messageContent);

			if (auditTaskData.getAuditTask() != null) {
				// update or insert audit task entity
				AuditTaskImpl updatedTask = auditTaskData.getAuditTask();
				AuditTaskImpl existingTask = getAuditTask(em, updatedTask.getTaskId());

				if (existingTask == null) {
					em.persist(updatedTask);
				} else {

					existingTask.setActivationTime(updatedTask.getActivationTime());
					existingTask.setActualOwner(updatedTask.getActualOwner());
					existingTask.setCreatedBy(updatedTask.getCreatedBy());
					existingTask.setCreatedOn(updatedTask.getCreatedOn());
					existingTask.setDeploymentId(updatedTask.getDeploymentId());
					existingTask.setDescription(updatedTask.getDescription());
					existingTask.setDueDate(updatedTask.getDueDate());
					existingTask.setLastModificationDate(updatedTask.getLastModificationDate());
					existingTask.setName(updatedTask.getName());
					existingTask.setParentId(updatedTask.getParentId());
					existingTask.setPriority(updatedTask.getPriority());
					existingTask.setProcessId(updatedTask.getProcessId());
					existingTask.setProcessInstanceId(updatedTask.getProcessInstanceId());
					existingTask.setProcessSessionId(updatedTask.getProcessSessionId());
					existingTask.setStatus(updatedTask.getStatus());
					existingTask.setTaskId(updatedTask.getTaskId());
					existingTask.setWorkItemId(updatedTask.getWorkItemId());

					em.merge(existingTask);
				}
			}

			if (auditTaskData.getTaskEvents() != null) {

				for (TaskEventImpl taskEvent : auditTaskData.getTaskEvents()) {
					em.persist(taskEvent);
				}
			}

			if (auditTaskData.getTaskInputs() != null) {

				for (TaskVariableImpl variable : auditTaskData.getTaskInputs()) {
					em.persist(variable);
				}
			}

			if (auditTaskData.getTaskOutputs() != null) {

				int removed = em.createNamedQuery("DeleteTaskVariableForTask")
						.setParameter("taskId", auditTaskData.getAuditTask().getTaskId())
						.setParameter("type", VariableType.OUTPUT).executeUpdate();
				logger.debug("Deleted {} output variables logs for task id {}", removed,
						auditTaskData.getAuditTask().getTaskId());

				for (TaskVariableImpl variable : auditTaskData.getTaskOutputs()) {
					em.persist(variable);
				}
			}

		} catch (JMSException e) {
			logger.error("Unexpected JMS error while processing task logs");
			throw new RuntimeException("Exception when receiving audit event event", e);
		}

	}

	protected AuditTaskImpl getAuditTask(EntityManager em, long taskId) {
		try {
			AuditTaskImpl auditTaskImpl = (AuditTaskImpl) em.createNamedQuery("getAuditTaskById")
					.setParameter("taskId", taskId).getSingleResult();

			return auditTaskImpl;
		} catch (NoResultException | NonUniqueResultException e) {
			return null;
		}
	}

}
