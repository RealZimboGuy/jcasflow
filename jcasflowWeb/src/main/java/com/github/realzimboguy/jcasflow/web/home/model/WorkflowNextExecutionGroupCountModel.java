package com.github.realzimboguy.jcasflow.web.home.model;

public class WorkflowNextExecutionGroupCountModel {

	private String time;
	private String group;
	private long count;

	public WorkflowNextExecutionGroupCountModel(String time, String group, long count) {

		this.time = time;
		this.group = group;
		this.count = count;
	}

	public String getTime() {

		return time;
	}

	public void setTime(String time) {

		this.time = time;
	}

	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}

	public long getCount() {

		return count;
	}

	public void setCount(long count) {

		this.count = count;
	}
}
