package com.github.realzimboguy.casflow.repo.entity;

import com.datastax.oss.driver.api.core.cql.ResultSet;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class WorkflowNextExecutionEntity {

	private String  group;
	private Instant nextExecution;
	private UUID    workflowId;


	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}

	public Instant getNextExecution() {

		return nextExecution;
	}

	public void setNextExecution(Instant nextExecution) {

		this.nextExecution = nextExecution;
	}

	public UUID getWorkflowId() {

		return workflowId;
	}

	public void setWorkflowId(UUID workflowId) {

		this.workflowId = workflowId;
	}
}
