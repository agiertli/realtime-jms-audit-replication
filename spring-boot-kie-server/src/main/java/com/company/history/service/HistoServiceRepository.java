package com.company.history.service;

import javax.persistence.EntityManagerFactory;

import org.drools.persistence.api.TransactionManager;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
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
	public RuntimeDataService getHistoryRuntimeDataService() {

		logger.info("Instantiating historyRuntimeDataService");
		TransactionManager kieTransactionManager = new KieSpringTransactionManager(
				(AbstractPlatformTransactionManager) transactionManager);
		TransactionalCommandService tcs = new SpringTransactionalCommandService(emf, kieTransactionManager,
				(AbstractPlatformTransactionManager) transactionManager);
		RuntimeDataService runtimeDataService = new RuntimeDataServiceImpl();
		((RuntimeDataServiceImpl) runtimeDataService).setCommandService(tcs);
		return runtimeDataService;
	}

	@Bean(name = "historyRuntimeDataServiceBase")
	public RuntimeDataServiceBase getHistoryRuntimeDataServiceBase(RuntimeDataService historyRuntimeDataService) {

		return new RuntimeDataServiceBase(historyRuntimeDataService, ((KieServerImpl) server).getServerRegistry());

	}

}
