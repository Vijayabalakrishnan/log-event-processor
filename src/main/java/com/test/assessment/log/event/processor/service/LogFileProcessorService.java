package com.test.assessment.log.event.processor.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.test.assessment.log.event.processor.constants.EventState;
import com.test.assessment.log.event.processor.entity.LogEvents;
import com.test.assessment.log.event.processor.model.LogEvent;
import com.test.assessment.log.event.processor.repository.LogEventsRepository;

@Service
public class LogFileProcessorService implements FileProcessorService {

	private static Logger LOGGER = LogManager.getLogger(LogFileProcessorService.class);

	private Gson gson;

	private int alertThreshold;

	private LogEventsRepository logEventsRepository;

	private Map<String, LogEvent> logEventMap = new HashMap<>();

	private Map<String, List<LogEvent>> persistanceFailedLogEventMap = new HashMap<>();

	private List<LogEvent> ignoredLogEventList = new ArrayList<>();

	@Autowired
	public LogFileProcessorService(@Value("${alert.threshold:4}") int alertThreshold, Gson gson,
			LogEventsRepository logEventsRepository) {

		this.gson = gson;
		this.alertThreshold = alertThreshold;
		this.logEventsRepository = logEventsRepository;
	}

	public Map<String, LogEvent> getLogEventMap() {
		return logEventMap;
	}

	public Map<String, List<LogEvent>> getPersistanceFailedLogEventMap() {
		return persistanceFailedLogEventMap;
	}

	public List<LogEvent> getIgnoredLogEventList() {
		return ignoredLogEventList;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void processFile(String filePath) {

		LOGGER.debug("File Path: {}", filePath);

		LogEvent logEvent = null;
		LineIterator lineIterator = null;

		try {

			lineIterator = FileUtils.lineIterator(new File(filePath), "UTF-8");

			while (lineIterator.hasNext()) {

				String line = lineIterator.nextLine();

				logEvent = parseLogLine(line);

				if (logEvent != null && isValidLogEvent(logEvent)) {
					processEvent(logEvent);
				} else {
					addToIgnoredLogEventList(logEvent);
				}
			}

			// For tracking purpose only
			logUnprocessedEvents();

		} catch (IOException e) {
			LOGGER.error("Exception while reading the file.", e);
		} finally {
			LineIterator.closeQuietly(lineIterator);
		}
	}

	private LogEvent parseLogLine(String line) {

		LogEvent logEvent = null;

		try {

			logEvent = this.gson.fromJson(line, LogEvent.class);
		} catch (Exception e) {
			LOGGER.error("Unable to parse the file {}", line, e);
		}

		return logEvent;
	}

	private boolean isValidLogEvent(LogEvent logEvent) {
		return StringUtils.isNotBlank(logEvent.getId()) && logEvent.getTimestamp() != null;
	}

	private void processEvent(LogEvent logEvent) {

		String eventId = logEvent.getId();

		LogEvent storedEvent = this.logEventMap.get(eventId);
		if (storedEvent != null) {

			try {

				storeLogEvent(storedEvent, logEvent);

			} catch (Exception e) {

				// Save failed events for tracking purpose
				addToPersistanceFailedMap(storedEvent, logEvent);
				LOGGER.error("Exception while inserting the record: ", e);
			}

			// Remove from map after processing
			this.logEventMap.remove(eventId);

		} else {

			// Event Id doesn't exists in Map
			this.logEventMap.put(eventId, logEvent);
		}
	}

	private void storeLogEvent(LogEvent storedEvent, LogEvent newEvent) {

		String eventId = storedEvent.getId();
		int eventDuration = getEventDuration(storedEvent, newEvent).intValue();
		boolean hasAlert = eventDuration > this.alertThreshold;
		String eventType = (storedEvent.getType() != null) ? storedEvent.getType() : newEvent.getType();
		String host = (storedEvent.getHost() != null) ? storedEvent.getHost() : newEvent.getHost();

		LOGGER.debug("Event id {}, type {}, duration {}, host {}, alert {}", eventId, eventType, eventDuration, host,
				hasAlert);

		this.logEventsRepository.save(new LogEvents(eventId, eventType, eventDuration, host, hasAlert));
	}

	private Long getEventDuration(LogEvent existingEvent, LogEvent newEvent) {

		return EventState.STARTED.equals(existingEvent.getState())
				? newEvent.getTimestamp() - existingEvent.getTimestamp()
				: existingEvent.getTimestamp() - newEvent.getTimestamp();
	}

	private void addToPersistanceFailedMap(LogEvent logEvent, LogEvent matchingLogEvent) {
		this.persistanceFailedLogEventMap.put(logEvent.getId(), Arrays.asList(logEvent, matchingLogEvent));
	}

	private void addToIgnoredLogEventList(LogEvent logEvent) {
		this.ignoredLogEventList.add(logEvent);
	}

	private void logUnprocessedEvents() {

		if (!this.logEventMap.isEmpty()) {
			LOGGER.warn("Couldn't find matching event for id(s) {}", this.logEventMap.keySet());
		}

		if (!this.persistanceFailedLogEventMap.isEmpty()) {
			LOGGER.warn("Failed to persist event with id(s) {}", this.persistanceFailedLogEventMap.keySet());
		}

		if (!this.ignoredLogEventList.isEmpty()) {
			LOGGER.warn("Invalid events(s) {}", this.ignoredLogEventList);
		}
	}
}