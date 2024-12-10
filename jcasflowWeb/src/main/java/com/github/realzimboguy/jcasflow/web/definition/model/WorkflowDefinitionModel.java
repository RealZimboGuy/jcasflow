package com.github.realzimboguy.jcasflow.web.definition.model;

public class WorkflowDefinitionModel {


	private String group;
	private String name;
	private String flowChart;
	private String created;
	private String updated;


	public WorkflowDefinitionModel() {

	}

	public WorkflowDefinitionModel(String  group, String name, String flowChart, String created, String updated) {

		this.group = group;
		this.name = name;
		this.flowChart = flowChart;
		this.created = created;
		this.updated = updated;
	}

	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getFlowChart() {

		return flowChart;
	}

	public void setFlowChart(String flowChart) {

		this.flowChart = flowChart;
	}

	public String getCreated() {

		return created;
	}

	public void setCreated(String created) {

		this.created = created;
	}

	public String getUpdated() {

		return updated;
	}

	public void setUpdated(String updated) {

		this.updated = updated;
	}
}
