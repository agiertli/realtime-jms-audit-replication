package com.company.history.service;

import javax.persistence.EntityManagerFactory;

import org.drools.persistence.api.TransactionManager;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.spring.jbpm.services.SpringTransactionalCommandService;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Component
public class HistoServiceRepository {

	Logger logger = LoggerFactory.getLogger(HistoServiceRepository.class);

	@Autowired
	@Qualifier("auditEntityManager")
	EntityManagerFactory emf;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private KieServer server;

	@Bean(name = "historyRuntimeDataService")
	public CustomRuntimeDataServiceImpl getHistoryRuntimeDataService() {

		logger.info("Instantiating CustomRuntimeDataServiceImpl");
		TransactionManager kieTransactionManager = new KieSpringTransactionManager(
				(AbstractPlatformTransactionManager) transactionManager);
		TransactionalCommandService tcs = new SpringTransactionalCommandService(emf, kieTransactionManager,
				(AbstractPlatformTransactionManager) transactionManager);
		CustomRuntimeDataServiceImpl runtimeDataService = new CustomRuntimeDataServiceImpl();
		((CustomRuntimeDataServiceImpl) runtimeDataService).setCommandService(tcs);
		return runtimeDataService;
	}

	@Bean(name = "historyRuntimeDataServiceBase")
	public CustomRuntimeDataServiceBase getHistoryRuntimeDataServiceBase(
			CustomRuntimeDataServiceImpl historyRuntimeDataService) {
		logger.info("Instantiating CustomRuntimeDataServiceBase");


		return new CustomRuntimeDataServiceBase(historyRuntimeDataService,
				((KieServerImpl) server).getServerRegistry());

	}

}
