package com.company.service;

import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.h2.jdbcx.JdbcDataSource;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "auditEntityManager", transactionManagerRef = "transactionManager", basePackages = {
		"com.company.service" })
public class JPAAuditBean {

	@Autowired
	private TransactionManager tm;

	protected static final String AUDIT_PERSISTENCE_UNIT_NAME = "org.jbpm.audit.persistence.jpa";
	protected static final String PERSISTENCE_XML_LOCATION = "classpath:/META-INF/audit-persistence.xml";

	@Bean(name = "auditDataSource")
	public XADataSource h2DataSource() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:audit-jbpm");
		ds.setUser("sa");
		ds.setPassword("sa");
		return ds;
	}

	@Bean
	public DataSource auditDatasource() {
		DataSourceXAConnectionFactory dataSourceXAConnectionFactory = new DataSourceXAConnectionFactory(tm,
				h2DataSource());
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
				dataSourceXAConnectionFactory, null);
		GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
		poolableConnectionFactory.setPool(connectionPool);
		return new ManagedDataSource<>(connectionPool, dataSourceXAConnectionFactory.getTransactionRegistry());
	}

	@Bean(name = "auditJPAVendorAdapter")
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		jpaVendorAdapter.setGenerateDdl(true);
		jpaVendorAdapter.setShowSql(true);
		jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
		return jpaVendorAdapter;
	}

	public Properties jpaAuditProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		properties.setProperty("hibernate.show_sql", "true");
		properties.setProperty("hibernate.max_fetch_depth", "3");
		properties.setProperty("hibernate.jdbc.fetch_size", "100");
		properties.setProperty("hibernate.ddl-auto", "update");
		properties.setProperty("hibernate.id.new_generator_mappings", "false");
		return properties;
	}

	@Bean(name = "auditEntityManager")
	public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(JpaProperties jpaProperties) {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName(AUDIT_PERSISTENCE_UNIT_NAME);
		factoryBean.setPersistenceXmlLocation(PERSISTENCE_XML_LOCATION);
		factoryBean.setJtaDataSource(auditDatasource());
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
		factoryBean.setJpaProperties(jpaAuditProperties());
		return factoryBean;
	}

	@Bean(name = "auditJDBCTemplate")
	public JdbcTemplate jdbcTemplate1(@Qualifier("auditDataSource") DataSource ds) {
		return new JdbcTemplate(ds);
	}

}
