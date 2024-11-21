package com.github.realzimboguy.casflow.executor;

import com.github.realzimboguy.casflow.workflow.JCasWorkFlow;

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
