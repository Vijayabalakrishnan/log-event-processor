package com.test.assessment.log.event.processor.service;

import java.io.File;
import java.io.IOException;
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
import com.test.assessment.log.event.processor.LogEventsRepository;
import com.test.assessment.log.event.processor.constants.EventState;
import com.test.assessment.log.event.processor.entity.LogEvents;
import com.test.assessment.log.event.processor.exception.ValidationException;
import com.test.assessment.log.event.processor.model.LogEvent;

@Service
public class LogFileProcessorService implements FileProcessorService {

	private static Logger LOGGER = LogManager.getLogger(LogFileProcessorService.class);

	private Gson gson;

	private int alertThreshold;

	private LogEventsRepository logEventsRepository;

	private Map<String, LogEvent> logEventMap = new HashMap<>();

	@Autowired
	public LogFileProcessorService(@Value("${alert.threshold:4}") int alertThreshold, Gson gson,
			LogEventsRepository logEventsRepository) {

		this.gson = gson;
		this.alertThreshold = alertThreshold;
		this.logEventsRepository = logEventsRepository;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void processFile(String filePath) {

		LOGGER.debug("File Path: {}", filePath);

		if (StringUtils.isNotBlank(filePath)) {

			LogEvent logEvent = null;
			LineIterator lineIterator = null;

			try {

				lineIterator = FileUtils.lineIterator(new File(filePath), "UTF-8");

				while (lineIterator.hasNext()) {

					String line = lineIterator.nextLine();
					// System.out.println(line);

					logEvent = parseLogLine(line);

					if (logEvent != null) {
						processEvent(parseLogLine(line));
					}
				}

				List<LogEvents> events = this.logEventsRepository.findAll();
				events.forEach(event -> LOGGER.info(event.toString()));

				logOrphanEvents();

			} catch (IOException e) {
				LOGGER.error("Exception while reading the file.", e);
			} finally {
				LineIterator.closeQuietly(lineIterator);
			}
		} else {
			throw new ValidationException("Input log file must be valid");
		}
	}

	private LogEvent parseLogLine(String line) {
		return this.gson.fromJson(line, LogEvent.class);
	}

	private void processEvent(LogEvent logEvent) {

		String eventId = logEvent.getId();

		if (StringUtils.isNotBlank(eventId) && logEvent.getTimestamp() != null) {

			LogEvent storedEvent = this.logEventMap.get(eventId);
			if (storedEvent != null) {

				storeLogEvent(storedEvent, logEvent);
				// Remove from map to avoid space issues
				this.logEventMap.remove(eventId);

			} else {

				// Event Id doesn't exists in Map
				this.logEventMap.put(eventId, logEvent);
			}

		} else {
			LOGGER.warn("Event Id or Timestamp is null. So ignoring the event");
		}
	}

	private void storeLogEvent(LogEvent storedEvent, LogEvent newEvent) {

		int eventDuration = getEventDuration(storedEvent, newEvent).intValue();
		boolean hasAlert = eventDuration > this.alertThreshold;
		String eventType = (storedEvent.getType() != null) ? storedEvent.getType() : newEvent.getType();
		String host = (storedEvent.getHost() != null) ? storedEvent.getHost() : newEvent.getHost();

		LOGGER.debug("Event Duration for id {} is {}", storedEvent.getId(), eventDuration);

		this.logEventsRepository.save(new LogEvents(storedEvent.getId(), eventType, eventDuration, host, hasAlert));
	}

	private Long getEventDuration(LogEvent existingEvent, LogEvent newEvent) {

		return EventState.STARTED.equals(existingEvent.getState())
				? newEvent.getTimestamp() - existingEvent.getTimestamp()
				: existingEvent.getTimestamp() - newEvent.getTimestamp();
	}

	private void logOrphanEvents() {

		if (!this.logEventMap.isEmpty()) {

			LOGGER.warn("Couldn't find matching event for id(s) {}", this.logEventMap.keySet());
		}
	}
}