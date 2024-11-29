package com.github.realzimboguy.jcasflow.engine.executor;

import java.time.Duration;

public class WorkflowRetry {

	private int maxFails;
	private Duration minInterval;
	private Duration maxInterval;

	public WorkflowRetry(int maxFails, Duration minInterval, Duration maxInterval) {

		this.maxFails = maxFails;
		this.minInterval = minInterval;
		this.maxInterval = maxInterval;
	}

	public int getMaxFails() {

		return maxFails;
	}

	public Duration getMinInterval() {

		return minInterval;
	}

	public Duration getMaxInterval() {

		return maxInterval;
	}
}
