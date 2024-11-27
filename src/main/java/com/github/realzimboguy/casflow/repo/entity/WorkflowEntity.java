package com.github.realzimboguy.casflow.repo.entity;

import com.github.realzimboguy.casflow.executor.WorkflowStatus;

import java.time.Instant;
import java.util.UUID;

public class WorkflowEntity {

	private UUID           id;
	private WorkflowStatus status;
	private int            executionCount;
	private int            retryCount;
	private Instant        created;
	private Instant          modified;
	private Instant nextActivation;
	private Instant started;
	private String executorId;
	private String executorGroup;
	private String workflowType;
	private String externalId;
	private String businessKey;
	private String state;
	private String stateVars;


	public UUID getId() {

		return id;
	}

	public void setId(UUID id) {

		this.id = id;
	}

	public WorkflowStatus getStatus() {

		return status;
	}

	public void setStatus(WorkflowStatus status) {

		this.status = status;
	}

	public int getExecutionCount() {

		return executionCount;
	}

	public void setExecutionCount(int executionCount) {

		this.executionCount = executionCount;
	}

	public Instant getCreated() {

		return created;
	}

	public void setCreated(Instant created) {

		this.created = created;
	}

	public Instant getModified() {

		return modified;
	}

	public void setModified(Instant modified) {

		this.modified = modified;
	}

	public Instant getNextActivation() {

		return nextActivation;
	}

	public void setNextActivation(Instant nextActivation) {

		this.nextActivation = nextActivation;
	}

	public Instant getStarted() {

		return started;
	}

	public void setStarted(Instant started) {

		this.started = started;
	}

	public String getExecutorId() {

		return executorId;
	}

	public void setExecutorId(String executorId) {

		this.executorId = executorId;
	}

	public String getWorkflowType() {

		return workflowType;
	}

	public void setWorkflowType(String workflowType) {

		this.workflowType = workflowType;
	}

	public String getExternalId() {

		return externalId;
	}

	public void setExternalId(String externalId) {

		this.externalId = externalId;
	}

	public String getBusinessKey() {

		return businessKey;
	}

	public void setBusinessKey(String businessKey) {

		this.businessKey = businessKey;
	}

	public String getState() {

		return state;
	}

	public void setState(String state) {

		this.state = state;
	}

	public String getStateVars() {

		return stateVars;
	}

	public void setStateVars(String stateVars) {

		this.stateVars = stateVars;
	}

	public String getExecutorGroup() {

		return executorGroup;
	}

	public void setExecutorGroup(String executorGroup) {

		this.executorGroup = executorGroup;
	}

	public int getRetryCount() {

		return retryCount;
	}

	public void setRetryCount(int retryCount) {

		this.retryCount = retryCount;
	}

	@Override
	public String toString() {

		return "WorkflowEntity{" +
				"id=" + id +
				", status=" + status +
				", executionCount=" + executionCount +
				", retryCount=" + retryCount +
				", created=" + created +
				", modified=" + modified +
				", nextActivation=" + nextActivation +
				", started=" + started +
				", executorId='" + executorId + '\'' +
				", executorGroup='" + executorGroup + '\'' +
				", workflowType='" + workflowType + '\'' +
				", externalId='" + externalId + '\'' +
				", businessKey='" + businessKey + '\'' +
				", state='" + state + '\'' +
				", stateVars='" + stateVars + '\'' +
				'}';
	}
}
