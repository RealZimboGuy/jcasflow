package com.github.realzimboguy.casflow.repo.entity;

import com.github.realzimboguy.casflow.executor.WorkflowActionType;
import com.github.realzimboguy.casflow.executor.WorkflowStatus;

import java.time.Instant;
import java.util.UUID;

public class WorkflowActionEntity {

	private UUID               workflowId;
	private UUID               id;
	private int                executionCount;
	private int            retryCount;
	private WorkflowActionType type;
	private String name;
	private String text;
	private Instant        date_time;

	public WorkflowActionEntity() {

	}

	public WorkflowActionEntity(UUID workflowId, UUID id, int executionCount, int retryCount, WorkflowActionType type, String name, String text, Instant date_time) {

		this.workflowId = workflowId;
		this.id = id;
		this.executionCount = executionCount;
		this.retryCount = retryCount;
		this.type = type;
		this.name = name;
		this.text = text;
		this.date_time = date_time;
	}

	public UUID getWorkflowId() {

		return workflowId;
	}

	public void setWorkflowId(UUID workflowId) {

		this.workflowId = workflowId;
	}

	public UUID getId() {

		return id;
	}

	public void setId(UUID id) {

		this.id = id;
	}

	public int getExecutionCount() {

		return executionCount;
	}

	public void setExecutionCount(int executionCount) {

		this.executionCount = executionCount;
	}

	public int getRetryCount() {

		return retryCount;
	}

	public void setRetryCount(int retryCount) {

		this.retryCount = retryCount;
	}

	public WorkflowActionType getType() {

		return type;
	}

	public void setType(WorkflowActionType type) {

		this.type = type;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getText() {

		return text;
	}

	public void setText(String text) {

		this.text = text;
	}

	public Instant getDate_time() {

		return date_time;
	}

	public void setDate_time(Instant date_time) {

		this.date_time = date_time;
	}

	@Override
	public String toString() {

		return "WorkflowActionEntity{" +
				"workflowId=" + workflowId +
				", id=" + id +
				", executionCount=" + executionCount +
				", retryCount=" + retryCount +
				", type=" + type +
				", name='" + name + '\'' +
				", text='" + text + '\'' +
				", date_time=" + date_time +
				'}';
	}
}
