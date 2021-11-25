package com.test.assessment.log.event.processor.model;

import com.test.assessment.log.event.processor.constants.EventState;

public class LogEvent {

	private String id;

	private String type;

	private Long timestamp;

	private String host;

	private EventState state;

	public LogEvent(String id, String type, Long timestamp, String host, EventState state) {

		this.id = id;
		this.type = type;
		this.timestamp = timestamp;
		this.host = host;
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public EventState getState() {
		return state;
	}

	public void setState(EventState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "LogEvent [id=" + id + ", type=" + type + ", timestamp=" + timestamp + ", host=" + host
				+ ", state=" + state + "]";
	}
}