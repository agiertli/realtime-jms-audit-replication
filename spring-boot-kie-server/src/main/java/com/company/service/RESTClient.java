package com.company.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RESTClient {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
