package com.github.realzimboguy.jcasflow.engine.executor;

public interface WorkflowState {


	WorkflowStateType stateType();

	String method();

	String name();

	public enum WorkflowStateType{
		START,
		NORMAL,
		END,
		MANUAL,
		ERROR
	}

}
