package com.github.realzimboguy.jcasflow.engine.repo.entity;

import java.time.Instant;
import java.util.UUID;

public class WorkflowByTypeEntity {

	private String group;
	private String workflowType;
	private UUID		  workflowId;
	private Instant        created;
	private String externalId;
	private String businessKey;
	private String status;
	private String state;

	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}

	public Instant getCreated() {

		return created;
	}

	public void setCreated(Instant created) {

		this.created = created;
	}

	public UUID getWorkflowId() {

		return workflowId;
	}

	public void setWorkflowId(UUID workflowId) {

		this.workflowId = workflowId;
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


	public String getStatus() {

		return status;
	}

	public void setStatus(String status) {

		this.status = status;
	}

	public String getState() {

		return state;
	}

	public void setState(String state) {

		this.state = state;
	}

	@Override
	public String toString() {

		return "WorkflowByTypeEntity{" +
				"group='" + group + '\'' +
				", workflowType='" + workflowType + '\'' +
				", workflowId=" + workflowId +
				", created=" + created +
				", externalId='" + externalId + '\'' +
				", businessKey='" + businessKey + '\'' +
				", status='" + status + '\'' +
				", state='" + state + '\'' +
				'}';
	}
}
