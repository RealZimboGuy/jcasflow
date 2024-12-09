package com.github.realzimboguy.jcasflow.engine.repo.entity;

import com.datastax.oss.driver.api.core.type.DataTypes;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowStatus;

import java.time.Instant;
import java.util.UUID;

public class WorkflowCreatedEntity {

	private String group;
	private Instant        created;
	private UUID		  workflowId;
	private String workflowType;
	private String externalId;
	private String businessKey;

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

	@Override
	public String toString() {

		return "WorkflowCreatedEntity{" +
				"group='" + group + '\'' +
				", created=" + created +
				", workflowId=" + workflowId +
				", workflowType='" + workflowType + '\'' +
				", externalId='" + externalId + '\'' +
				", businessKey='" + businessKey + '\'' +
				'}';
	}
}
