package com.github.realzimboguy.jcasflow.engine.repo.entity;

import java.time.Instant;
import java.util.UUID;

public class WorkflowNextExecutionGroupCountEntity {

	private String  group;
	private long count;

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
