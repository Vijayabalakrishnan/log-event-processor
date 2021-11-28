package com.test.assessment.log.event.processor.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	LogFileProcessorService logFileProcessorService;

	Gson gson = new Gson();

	LogEventsRepository logEventsRepository;

	ArgumentCaptor<LogEvents> logEventsArgumentCaptor;

	@BeforeEach
	public void init() {

		logEventsRepository = mock(LogEventsRepository.class);
		logEventsArgumentCaptor = ArgumentCaptor.forClass(LogEvents.class);
		logFileProcessorService = new LogFileProcessorService(4, gson, logEventsRepository);
	}

	@Test
	public void testProcessFile_validFile() {

		logFileProcessorService.processFile("src/test/resources/files/valid-events.txt");

		verify(logEventsRepository, times(2)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(2)).save(any());

		List<LogEvents> logEvents = logEventsArgumentCaptor.getAllValues();
		assertNotNull(logEvents);
		verifyLogEvents(logEvents.get(0), "test1", "App Log", "127.0.0.0", 3, false);
		verifyLogEvents(logEvents.get(1), "test2", "App Log", null, 7, true);

		assertTrue(logFileProcessorService.getLogEventMap().isEmpty());
		assertTrue(logFileProcessorService.getIgnoredLogEventList().isEmpty());
		assertTrue(logFileProcessorService.getPersistanceFailedLogEventMap().isEmpty());
	}

	@Test
	public void testProcessFile_invalidJsonContent() {

		logFileProcessorService.processFile("src/test/resources/files/invalid-json-format-event.txt");

		verify(logEventsRepository, times(0)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(0)).save(any());

		assertTrue(logFileProcessorService.getLogEventMap().isEmpty());
		assertFalse(logFileProcessorService.getIgnoredLogEventList().isEmpty());
		assertTrue(logFileProcessorService.getIgnoredLogEventList().size() == 1);
		assertTrue(logFileProcessorService.getPersistanceFailedLogEventMap().isEmpty());
	}

	@Test
	public void testProcessFile_invalidEvents() {

		logFileProcessorService.processFile("src/test/resources/files/invalid-events.txt");

		verify(logEventsRepository, times(0)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(0)).save(any());

		assertFalse(logFileProcessorService.getLogEventMap().isEmpty());
		assertTrue(logFileProcessorService.getLogEventMap().size() == 1);
		assertFalse(logFileProcessorService.getIgnoredLogEventList().isEmpty());
		assertTrue(logFileProcessorService.getIgnoredLogEventList().size() == 2);
		assertTrue(logFileProcessorService.getPersistanceFailedLogEventMap().isEmpty());
	}

	@Test
	public void testProcessFile_invalidFilePath() {

		logFileProcessorService.processFile("src/test/resources/files/dummy-name.txt");

		verify(logEventsRepository, times(0)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(0)).save(any());

		assertTrue(logFileProcessorService.getLogEventMap().isEmpty());
		assertTrue(logFileProcessorService.getIgnoredLogEventList().isEmpty());
		assertTrue(logFileProcessorService.getPersistanceFailedLogEventMap().isEmpty());
	}

	@Test
	public void testProcessFile_saveFailure() {

		doThrow(new RuntimeException()).when(logEventsRepository).save(any(LogEvents.class));
		logFileProcessorService.processFile("src/test/resources/files/valid-events.txt");

		verify(logEventsRepository, times(2)).save(logEventsArgumentCaptor.capture());
		verify(logEventsRepository, times(2)).save(any());

		assertTrue(logFileProcessorService.getLogEventMap().isEmpty());
		assertTrue(logFileProcessorService.getIgnoredLogEventList().isEmpty());
		assertFalse(logFileProcessorService.getPersistanceFailedLogEventMap().isEmpty());
		assertTrue(logFileProcessorService.getPersistanceFailedLogEventMap().size() == 2);
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