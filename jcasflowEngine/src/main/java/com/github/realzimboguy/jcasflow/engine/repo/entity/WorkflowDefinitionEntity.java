package com.github.realzimboguy.jcasflow.engine.repo.entity;

import java.time.Instant;
import java.util.UUID;

public class WorkflowDefinitionEntity {

	private String  group;
	private String  name;
	private String  description;
	private Instant created;
	private Instant updated;
	private String  flowChart;

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public Instant getUpdated() {

		return updated;
	}

	public void setUpdated(Instant updated) {

		this.updated = updated;
	}

	public String getFlowChart() {

		return flowChart;
	}

	public void setFlowChart(String flowChart) {

		this.flowChart = flowChart;
	}

	public Instant getCreated() {

		return created;
	}

	public void setCreated(Instant created) {

		this.created = created;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {

		this.description = description;
	}

	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}
}
