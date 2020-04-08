package com.company.history.jms.producers;

import org.jbpm.services.task.audit.TaskLifeCycleEventConstants;
import org.jbpm.services.task.audit.impl.model.AuditTaskData;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.jbpm.services.task.audit.impl.model.TaskVariableImpl;
import org.jbpm.services.task.audit.variable.TaskIndexerManager;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.jbpm.services.task.persistence.PersistableEventListener;
import org.jbpm.services.task.utils.ClassUtil;

import static org.kie.soup.commons.xstream.XStreamUtils.createTrustingXStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.TextMessage;

import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.TaskVariable;
import org.kie.internal.task.api.TaskVariable.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

@Component
public class JMSTaskEventListener extends PersistableEventListener implements TaskLifeCycleEventListener {

	private static final Logger logger = LoggerFactory.getLogger(JMSTaskEventListener.class);
	private XStream xstream;
	@Autowired
	private JmsTemplate jmsTemplate;

	@Value("${audit.queue}")
	private String queue;

	public JMSTaskEventListener() {
		super(null);
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
	public void afterTaskStartedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();

		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.STARTED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);
		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 5);
	}

	@Override
	public void afterTaskActivatedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.ACTIVATED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 8);
	}

	@Override
	public void afterTaskClaimedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.CLAIMED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());

		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 8);

	}

	@Override
	public void afterTaskSkippedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.SKIPPED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());

		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 2);
	}

	@Override
	public void afterTaskStoppedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.STOPPED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 4);
	}

	@Override
	public void afterTaskCompletedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.COMPLETED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 2);
	}

	@Override
	public void afterTaskFailedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.FAILED, ti.getTaskData().getProcessInstanceId(),
				ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 2);
	}

	@Override
	public void afterTaskAddedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		if (ti.getTaskData().getProcessId() != null) {
			userId = ti.getTaskData().getProcessId();
		}
		AuditTaskImpl auditTask = createAuditTask(ti, event.getEventDate());
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.ADDED, ti.getTaskData().getProcessInstanceId(),
				ti.getTaskData().getWorkItemId(), userId);
		sendMessage(new AuditTaskData(auditTask, taskEvent), 9);
	}

	@Override
	public void afterTaskExitedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.EXITED, ti.getTaskData().getProcessInstanceId(),
				ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 2);
	}

	@Override
	public void afterTaskReleasedEvent(TaskEvent event) {
		Task ti = event.getTask();
		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());

		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl), 8);
	}

	@Override
	public void afterTaskResumedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.RESUMED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 6);
	}

	@Override
	public void afterTaskSuspendedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.SUSPENDED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());

		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 6);
	}

	@Override
	public void afterTaskForwardedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();

		StringBuilder message = new StringBuilder();
		String entitiesAsString = (ti.getPeopleAssignments().getPotentialOwners()).stream().map(oe -> oe.getId())
				.collect(Collectors.joining(","));
		message.append("Forward to [" + entitiesAsString + "]");

		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.FORWARDED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId, message.toString());

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 4);
	}

	@Override
	public void afterTaskDelegatedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.DELEGATED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 4);
	}

	@Override
	public void afterTaskNominatedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.NOMINATED, userId, new Date());

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());
		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 4);
	}

	@Override
	public void beforeTaskReleasedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.RELEASED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);
		sendMessage(new AuditTaskData(null, taskEvent), 7);
	}

	protected AuditTaskImpl createAuditTask(Task ti, Date date) {
		AuditTaskImpl auditTaskImpl = new AuditTaskImpl(ti.getId(), ti.getName(), ti.getTaskData().getStatus().name(),
				ti.getTaskData().getActivationTime(),
				(ti.getTaskData().getActualOwner() != null) ? ti.getTaskData().getActualOwner().getId() : "",
				ti.getDescription(), ti.getPriority(),
				(ti.getTaskData().getCreatedBy() != null) ? ti.getTaskData().getCreatedBy().getId() : "",
				ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(),
				ti.getTaskData().getProcessSessionId(), ti.getTaskData().getDeploymentId(),
				ti.getTaskData().getParentId(), ti.getTaskData().getWorkItemId(), date);

		return auditTaskImpl;
	}

	@Override
	public void afterTaskUpdatedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();

		List<TaskEventImpl> taskEvents = new ArrayList<>();

		TaskPersistenceContext persistenceContext = getPersistenceContext(
				((TaskContext) event.getTaskContext()).getPersistenceContext());
		try {
			AuditTaskImpl auditTaskImpl = getAuditTask(persistenceContext, ti);
			if ((ti.getDescription() != null && !ti.getDescription().equals(auditTaskImpl.getDescription()))
					|| (ti.getDescription() == null && auditTaskImpl.getDescription() != null)) {
				String message = getUpdateFieldLog("Description", auditTaskImpl.getDescription(), ti.getDescription());

				TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
						org.kie.internal.task.api.model.TaskEvent.TaskEventType.UPDATED,
						ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId, message);
				taskEvents.add(taskEvent);
			}
			if ((ti.getName() != null && !ti.getName().equals(auditTaskImpl.getName()))
					|| (ti.getName() == null && auditTaskImpl.getName() != null)) {
				String message = getUpdateFieldLog("Name", auditTaskImpl.getName(), ti.getName());
				TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
						org.kie.internal.task.api.model.TaskEvent.TaskEventType.UPDATED,
						ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId, message);
				taskEvents.add(taskEvent);
			}
			if (auditTaskImpl.getPriority() != ti.getPriority()) {
				String message = getUpdateFieldLog("Priority", String.valueOf(auditTaskImpl.getPriority()),
						String.valueOf(ti.getPriority()));
				TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
						org.kie.internal.task.api.model.TaskEvent.TaskEventType.UPDATED,
						ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId, message);
				taskEvents.add(taskEvent);
			}

			if ((auditTaskImpl.getDueDate() != null && ti.getTaskData().getExpirationTime() != null
					&& auditTaskImpl.getDueDate().getTime() != ti.getTaskData().getExpirationTime().getTime())
					|| (auditTaskImpl.getDueDate() == null && ti.getTaskData().getExpirationTime() != null)
					|| (auditTaskImpl.getDueDate() != null && ti.getTaskData().getExpirationTime() == null)) {
				String fromDate = (auditTaskImpl.getDueDate() != null
						? new Date(auditTaskImpl.getDueDate().getTime()).toString()
						: null);
				String toDate = (ti.getTaskData().getExpirationTime() != null
						? ti.getTaskData().getExpirationTime().toString()
						: "");
				String message = getUpdateFieldLog("DueDate", fromDate, toDate);
				TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
						org.kie.internal.task.api.model.TaskEvent.TaskEventType.UPDATED,
						ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId, message);
				taskEvents.add(taskEvent);
			}

			auditTaskImpl.setDescription(ti.getDescription());
			auditTaskImpl.setName(ti.getName());
			auditTaskImpl.setPriority(ti.getPriority());
			auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
			auditTaskImpl.setLastModificationDate(event.getEventDate());

			sendMessage(new AuditTaskData(auditTaskImpl, taskEvents), 4);
		} finally {
			cleanup(persistenceContext);
		}
	}

	@Override
	public void afterTaskReassignedEvent(TaskEvent event) {
		String userId = event.getTaskContext().getUserId();
		Task ti = event.getTask();
		TaskEventImpl taskEvent = new TaskEventImpl(ti.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.DELEGATED,
				ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getWorkItemId(), userId);

		AuditTaskImpl auditTaskImpl = createAuditTask(ti, event.getEventDate());
		auditTaskImpl.setDescription(ti.getDescription());
		auditTaskImpl.setName(ti.getName());
		auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
		auditTaskImpl.setPriority(ti.getPriority());
		auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
		auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
		auditTaskImpl.setActualOwner(getActualOwner(ti));
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, taskEvent), 4);
	}

	@Override
	public void afterTaskOutputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
		String userId = event.getTaskContext().getUserId();
		Task task = event.getTask();

		if (variables == null || variables.isEmpty()) {
			return;
		}

		List<TaskVariableImpl> taskVariables = indexVariables(task, variables, VariableType.OUTPUT);
		String message = "Task output data updated";
		TaskEventImpl taskEvent = new TaskEventImpl(task.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.UPDATED,
				task.getTaskData().getProcessInstanceId(), task.getTaskData().getWorkItemId(), userId, message);
		AuditTaskImpl auditTaskImpl = createAuditTask(task, event.getEventDate());
		auditTaskImpl.setLastModificationDate(event.getEventDate());

		sendMessage(new AuditTaskData(auditTaskImpl, Collections.singletonList(taskEvent), null, taskVariables), 2);
	}

	@Override
	public void afterTaskInputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
		if (variables == null || variables.isEmpty()) {
			return;
		}
		Task task = event.getTask();
		List<TaskVariableImpl> taskVariables = indexVariables(task, variables, VariableType.INPUT);

		sendMessage(new AuditTaskData(null, null, taskVariables, null), 2);
	}

	protected List<TaskVariableImpl> indexVariables(Task task, Map<String, Object> variables, VariableType type) {
		TaskIndexerManager manager = TaskIndexerManager.get();
		List<TaskVariableImpl> taskVariables = new ArrayList<>();
		for (Map.Entry<String, Object> variable : variables.entrySet()) {
			if (TaskLifeCycleEventConstants.SKIPPED_TASK_VARIABLES.contains(variable.getKey())
					|| variable.getValue() == null) {
				continue;
			}
			List<TaskVariable> taskVars = manager.index(task, variable.getKey(), variable.getValue());

			if (taskVars != null) {
				for (TaskVariable tVariable : taskVars) {
					tVariable.setType(type);
					taskVariables.add((TaskVariableImpl) tVariable);
				}
			}
		}

		return taskVariables;
	}

	@Override
	public void afterTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type,
			List<OrganizationalEntity> entities) {
		assignmentsUpdated(event, type, entities, "] have been added");
	}

	@Override
	public void afterTaskAssignmentsRemovedEvent(TaskEvent event, AssignmentType type,
			List<OrganizationalEntity> entities) {
		assignmentsUpdated(event, type, entities, "] have been removed");
	}

	protected void assignmentsUpdated(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities,
			String messageSufix) {
		if (entities == null || entities.isEmpty()) {
			return;
		}
		String userId = event.getTaskContext().getUserId();
		Task task = event.getTask();

		StringBuilder message = new StringBuilder();

		switch (type) {
		case POT_OWNER:
			message.append("Potential owners [");
			break;
		case EXCL_OWNER:
			message.append("Excluded owners [");
			break;
		case ADMIN:
			message.append("Business administrators [");
			break;
		default:
			break;
		}
		String entitiesAsString = entities.stream().map(oe -> oe.getId()).collect(Collectors.joining(","));
		message.append(entitiesAsString);
		message.append(messageSufix);

		TaskEventImpl taskEvent = new TaskEventImpl(task.getId(),
				org.kie.internal.task.api.model.TaskEvent.TaskEventType.UPDATED,
				task.getTaskData().getProcessInstanceId(), task.getTaskData().getWorkItemId(), userId,
				message.toString());

		sendMessage(new AuditTaskData(null, taskEvent), 2);
	}

	protected String getUpdateFieldLog(String fieldName, String previousValue, String value) {
		return "Updated " + fieldName + " {From: '" + (previousValue != null ? previousValue : "") + "' to: '"
				+ (value != null ? value : "") + "'}";
	}

	protected String getActualOwner(Task ti) {
		String userId = "";
		if (ti.getTaskData().getActualOwner() != null) {
			userId = ti.getTaskData().getActualOwner().getId();
		}

		return userId;
	}

	protected AuditTaskImpl getAuditTask(TaskPersistenceContext persistenceContext, Task ti) {
		AuditTaskImpl auditTaskImpl = persistenceContext.queryWithParametersInTransaction("getAuditTaskById", true,
				persistenceContext.addParametersToMap("taskId", ti.getId()),
				ClassUtil.<AuditTaskImpl>castClass(AuditTaskImpl.class));

		return auditTaskImpl;
	}

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

	@Override
	public void beforeTaskActivatedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskClaimedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskSkippedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskStartedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskStoppedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskCompletedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskFailedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskAddedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskExitedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskResumedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskSuspendedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskForwardedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskDelegatedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTaskNominatedEvent(TaskEvent event) {
		// TODO Auto-generated method stub

	}

}
