package com.company.history.jms.receivers;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import org.jbpm.casemgmt.impl.jms.AsyncCaseInstanceAuditEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CaseAuditReceiver extends AsyncCaseInstanceAuditEventReceiver {

	private static final Logger logger = LoggerFactory.getLogger(CaseAuditReceiver.class);

	@Autowired
	@Qualifier("auditEntityManager")
	private EntityManagerFactory entityManagerFactory;

	public CaseAuditReceiver(EntityManagerFactory entityManagerFactory) {
		super(null);
	}

	@PostConstruct
	public void init() {

		logger.info("Setting case emf to parent");
		super.setEntityManagerFactory(entityManagerFactory);

	}

}
