package com.test.assessment.log.event.processor.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.test.assessment.log.event.processor.entity.LogEvents;
import com.test.assessment.log.event.processor.repository.LogEventsRepository;

@RunWith(MockitoJUnitRunner.class)
public class LogFileProcessorServiceUnitTest {

	FileProcessorService fileProcessorService;

	Gson gson = new Gson();

	LogEventsRepository logEventsRepository;

	ArgumentCaptor<LogEvents> logEventsArgumentCaptor;

	@BeforeEach
	public void init() {

		logEventsRepository = mock(LogEventsRepository.class);
		logEventsArgumentCaptor = ArgumentCaptor.forClass(LogEvents.class);
		fileProcessorService = new LogFileProcessorService(4, gson, logEventsRepository);
	}

	@Test
	public void testProcessFile_validFile() {

		fileProcessorService.processFile("src/test/resources/files/valid-events.txt");

		verify(logEventsRepository, times(2)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(2)).save(any());

		List<LogEvents> logEvents = logEventsArgumentCaptor.getAllValues();
		assertNotNull(logEvents);
		verifyLogEvents(logEvents.get(0), "test1", "App Log", "127.0.0.0", 3, false);
		verifyLogEvents(logEvents.get(1), "test2", "App Log", null, 7, true);
	}

	@Test
	public void testProcessFile_invalidJsonContent() {

		fileProcessorService.processFile("src/test/resources/files/invalid-json-format-event.txt");

		verify(logEventsRepository, times(0)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(0)).save(any());
	}

	@Test
	public void testProcessFile_invalidEvents() {

		fileProcessorService.processFile("src/test/resources/files/invalid-events.txt");

		verify(logEventsRepository, times(0)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(0)).save(any());
	}

	@Test
	public void testProcessFile_invalidFilePath() {

		fileProcessorService.processFile("src/test/resources/files/dummy-name.txt");

		verify(logEventsRepository, times(0)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(0)).save(any());
	}

	@Test
	public void testProcessFile_saveFailure() {

		doThrow(new RuntimeException()).when(logEventsRepository).save(any(LogEvents.class));
		fileProcessorService.processFile("src/test/resources/files/valid-events.txt");

		verify(logEventsRepository, times(2)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(2)).save(any());
	}

	private void verifyLogEvents(LogEvents logEvents, String id, String type, String host, int duration, boolean hasAlert) {

		assertNotNull(logEvents);
		assertEquals(id, logEvents.getEventId());
		assertEquals(type, logEvents.getEventType());
		assertEquals(host, logEvents.getEventHost());
		assertTrue(duration == logEvents.getEventDuration());
		assertEquals(hasAlert, logEvents.getAlert());
	}
}