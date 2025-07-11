package com.github.realzimboguy.jcasflow.engine.repo.entity;

import java.time.Instant;
import java.util.UUID;

public class WorkflowInProgressEntity {

	private String  group;
	private UUID    workflowId;
	private Instant startedAt;

	public WorkflowInProgressEntity() {

	}

	public WorkflowInProgressEntity(String group, UUID workflowId, Instant startedAt) {

		this.group = group;
		this.workflowId = workflowId;
		this.startedAt = startedAt;
	}

	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}

	public Instant getStartedAt() {

		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {

		this.startedAt = startedAt;
	}

	public UUID getWorkflowId() {

		return workflowId;
	}

	public void setWorkflowId(UUID workflowId) {

		this.workflowId = workflowId;
	}
}
