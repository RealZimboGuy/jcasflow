package com.github.realzimboguy.jcasflow.engine.repo.entity;

import java.time.Instant;
import java.util.UUID;

public class WorkflowNextExecutionEntity {

	private String  group;
	private Instant nextExecution;
	private UUID    workflowId;

	public WorkflowNextExecutionEntity() {

	}

	public WorkflowNextExecutionEntity(String group, Instant nextExecution, UUID workflowId) {

		this.group = group;
		this.nextExecution = nextExecution;
		this.workflowId = workflowId;
	}

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
