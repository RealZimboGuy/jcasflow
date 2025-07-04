package com.github.realzimboguy.jcasflow.engine.repo.entity;

import java.time.Instant;
import java.util.UUID;

public class ExecutorEntity {


	private UUID           id;
	private String  group;
	private String  host;
	private Instant startedAt;
	private Instant lastAlive;


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

	public Instant getStartedAt() {

		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {

		this.startedAt = startedAt;
	}

	public Instant getLastAlive() {

		return lastAlive;
	}

	public void setLastAlive(Instant lastAlive) {

		this.lastAlive = lastAlive;
	}

	public String getHost() {

		return host;
	}

	public void setHost(String host) {

		this.host = host;
	}

	@Override
	public String toString() {

		return "ExecutorEntity{" +
				"id=" + id +
				", group='" + group + '\'' +
				", host='" + host + '\'' +
				", startedAt=" + startedAt +
				", lastAlive=" + lastAlive +
				'}';
	}
}
