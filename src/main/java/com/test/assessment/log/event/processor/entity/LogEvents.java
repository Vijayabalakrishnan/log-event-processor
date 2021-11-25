package com.test.assessment.log.event.processor.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LOG_EVENTS")
public class LogEvents {

	@Id
	@Column(name = "EVENT_ID")
	private String eventId;

	@Column(name = "EVENT_TYPE")
	private String eventType;

	@Column(name = "EVENT_DURATION")
	private Integer eventDuration;

	@Column(name = "EVENT_HOST")
	private String eventHost;

	@Column(name = "ALERT")
	private Boolean alert;

	public LogEvents() {

	}

	public LogEvents(String eventId, String eventType, Integer eventDuration, String eventHost, Boolean alert) {

		this.eventId = eventId;
		this.eventType = eventType;
		this.eventDuration = eventDuration;
		this.eventHost = eventHost;
		this.alert = alert;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Integer getEventDuration() {
		return eventDuration;
	}

	public void setEventDuration(Integer eventDuration) {
		this.eventDuration = eventDuration;
	}

	public String getEventHost() {
		return eventHost;
	}

	public void setEventHost(String eventHost) {
		this.eventHost = eventHost;
	}

	public Boolean getAlert() {
		return alert;
	}

	public void setAlert(Boolean alert) {
		this.alert = alert;
	}

	@Override
	public String toString() {
		return "LogEvents [eventId=" + eventId + ", eventType=" + eventType + ", eventDuration=" + eventDuration
				+ ", eventHost=" + eventHost + ", alert=" + alert + "]";
	}
}