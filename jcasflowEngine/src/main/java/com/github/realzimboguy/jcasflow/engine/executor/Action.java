package com.github.realzimboguy.jcasflow.engine.executor;

import java.time.Duration;
import java.time.ZonedDateTime;

public class Action {

	private WorkflowState workflowState;
	private ZonedDateTime executeAt;
	private Duration duration;

	private Action(WorkflowState workflowState) {

		this.workflowState = workflowState;

	}

	public Action(WorkflowState workflowState, ZonedDateTime executeAt) {

		this.workflowState = workflowState;
		this.executeAt = executeAt;
	}

	public Action(WorkflowState workflowState,Duration duration) {

		this.workflowState = workflowState;
		this.duration = duration;
	}


	/**
	 * this is a synchronous action ie it does not hand off back to the pool, the same executor will continue to try process
	 * the workflow,this is the fastest possible execution
	 * @param workflowState
	 * @return
	 */
	public static Action nextState(WorkflowState workflowState) {

		return new Action(workflowState);

	}
	public static Action nextState(WorkflowState workflowState, ZonedDateTime executeAt) {

		return new Action(workflowState,executeAt);

	}	public static Action nextState(WorkflowState workflowState, Duration duration) {

		return new Action(workflowState,duration);

	}

	public WorkflowState getWorkflowState() {

		return workflowState;
	}

	public ZonedDateTime getExecuteAt() {

		return executeAt;
	}

	public Duration getDuration() {

		return duration;
	}

	@Override
	public String toString() {

		return "Action{" +
				"workflowState=" + workflowState +
				", executeAt=" + executeAt +
				", duration=" + duration +
				'}';
	}
}
