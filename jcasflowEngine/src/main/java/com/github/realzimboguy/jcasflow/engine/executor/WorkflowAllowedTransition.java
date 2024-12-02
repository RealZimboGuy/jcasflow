package com.github.realzimboguy.jcasflow.engine.executor;

public class WorkflowAllowedTransition {

	private WorkflowState from;
	private WorkflowState to;

	public WorkflowAllowedTransition(WorkflowState from, WorkflowState to) {

		this.from = from;
		this.to = to;
	}

	public WorkflowState getFrom() {

		return from;
	}

	public void setFrom(WorkflowState from) {

		this.from = from;
	}

	public WorkflowState getTo() {

		return to;
	}

	public void setTo(WorkflowState to) {

		this.to = to;
	}
}
