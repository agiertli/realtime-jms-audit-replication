package com.company.history.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jbpm.kie.services.impl.model.ProcessInstanceDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CustomQueryManager {

	Logger logger = LoggerFactory.getLogger(CustomQueryManager.class);
	@Autowired
	@Qualifier("auditEntityManager")
	EntityManagerFactory emf;

	public static final String ORDER_BY_KEY = "orderby";
	public static final String ASCENDING_KEY = "asc";
	public static final String DESCENDING_KEY = "desc";
	public static final String FILTER = "filter";

	private Map<String, String> queries = new ConcurrentHashMap<String, String>();

	public void parse(String ormFile) throws XMLStreamException {
		String name = null;
		StringBuffer tagContent = new StringBuffer();
		XMLInputFactory factory = XMLInputFactory.newInstance();

		Resource resource = new ClassPathResource(ormFile);
		logger.info("{} loaded : {}", ormFile, resource);
		InputStream input = null;
		try {
			input = resource.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		XMLStreamReader reader = factory.createXMLStreamReader(input);

		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if ("named-query".equals(reader.getLocalName())) {

					name = reader.getAttributeValue(0);
				}
				break;

			case XMLStreamConstants.CHARACTERS:
				if (name != null) {
					tagContent.append(reader.getText());
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if ("named-query".equals(reader.getLocalName())) {
					String origQuery = tagContent.toString();
					String alteredQuery = origQuery;
					int orderByIndex = origQuery.toLowerCase().indexOf("order by");
					if (orderByIndex != -1) {
						// remove order by clause as it will be provided on request
						alteredQuery = origQuery.substring(0, orderByIndex);
					}
					queries.put(name, alteredQuery);
					name = null;
					tagContent = new StringBuffer();
				}
				break;
			}
		}
	}

	public void addNamedQueries(String ormFile) {
		try {
			parse(ormFile);
		} catch (XMLStreamException e) {
			throw new RuntimeException("Unable to read orm file due to " + e.getMessage(), e);
		}
	}

	@PostConstruct
	public void init() {
		addNamedQueries("META-INF/history-Servicesorm.xml");
		addNamedQueries("META-INF/history-TaskAuditorm.xml");
		EntityManager tmpEM = emf.createEntityManager();
		queries.forEach((k, v) -> {
			logger.info("Registering {} query on history emf", k);
			Query q = tmpEM.createQuery(v);
			emf.addNamedQuery(k, q);
		});
		tmpEM.close();
	}
}
