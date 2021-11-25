package com.test.assessment.log.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.test.assessment.log.event.processor.service.FileProcessorService;

@SpringBootApplication
public class LogEventProcessorApplication implements CommandLineRunner {

	@Value("${log.file.path}")
	private String filePath;

	@Autowired
	private FileProcessorService fileProcessorService;

	public static void main(String[] args) {
		SpringApplication.run(LogEventProcessorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fileProcessorService.processFile(filePath);
	}
}