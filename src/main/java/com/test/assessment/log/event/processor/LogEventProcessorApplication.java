package com.test.assessment.log.event.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.test.assessment.log.event.processor.service.FileProcessorService;

@SpringBootApplication
public class LogEventProcessorApplication implements CommandLineRunner {

	private static Logger LOGGER = LogManager.getLogger(LogEventProcessorApplication.class);

	@Value("${log.file.path}")
	private String filePath;

	@Autowired
	private FileProcessorService fileProcessorService;

	public static void main(String[] args) {
		SpringApplication.run(LogEventProcessorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		if(StringUtils.isNotBlank(filePath)) {
			fileProcessorService.processFile(filePath);
		} else {
			LOGGER.error("Input file path must be valid");
		}
	}
}