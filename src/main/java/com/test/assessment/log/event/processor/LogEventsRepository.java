package com.test.assessment.log.event.processor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.assessment.log.event.processor.entity.LogEvents;

@Repository
public interface LogEventsRepository extends JpaRepository<LogEvents, Void> {

}