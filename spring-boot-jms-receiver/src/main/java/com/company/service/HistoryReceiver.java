package com.company.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryReceiver {

	Logger logger = LoggerFactory.getLogger(HistoryReceiver.class);

	@GetMapping("/history/plog/{pid}")
	public ResponseEntity historyPlog(@PathVariable("pid") Long pid) {

		logger.info("History request received");

		return new ResponseEntity("TestResponse", HttpStatus.OK);

	}

}
