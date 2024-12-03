package com.github.realzimboguy.jcasflow.web.executors.model;

import java.time.Instant;
import java.util.UUID;

public class ExecutorModel {

	private UUID    id;
	private     String  group;
	private     String  host;
	private     String startedAt;
	private     String lastAlive;
	private     String cssClass;

	public ExecutorModel(UUID id, String group, String host, String startedAt, String lastAlive, String cssClass) {

		this.id = id;
		this.group = group;
		this.host = host;
		this.startedAt = startedAt;
		this.lastAlive = lastAlive;
		this.cssClass = cssClass;
	}

	public ExecutorModel() {

	}

	public UUID getId() {

		return id;
	}

	public void setId(UUID id) {

		this.id = id;
	}

	public String getGroup() {

		return group;
	}

	public void setGroup(String group) {

		this.group = group;
	}

	public String getStartedAt() {

		return startedAt;
	}

	public void setStartedAt(String startedAt) {

		this.startedAt = startedAt;
	}

	public String getLastAlive() {

		return lastAlive;
	}

	public void setLastAlive(String lastAlive) {

		this.lastAlive = lastAlive;
	}

	public String getCssClass() {

		return cssClass;
	}

	public void setCssClass(String cssClass) {

		this.cssClass = cssClass;
	}

	public String getHost() {

		return host;
	}

	public void setHost(String host) {

		this.host = host;
	}
}
