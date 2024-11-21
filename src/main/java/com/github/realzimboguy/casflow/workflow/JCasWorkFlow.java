package com.github.realzimboguy.casflow.workflow;

import com.github.realzimboguy.casflow.executor.WorkflowState;

public abstract class JCasWorkFlow {

	public abstract String getName();

	public abstract WorkflowState startingState();

	public abstract WorkflowState errorState();


}
