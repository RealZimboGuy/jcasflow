package com.github.realzimboguy.casflow.workflow;

import com.github.realzimboguy.casflow.executor.WorkflowRetry;
import com.github.realzimboguy.casflow.executor.WorkflowState;

public abstract class JCasWorkFlow {

	public abstract String getName();

	public abstract WorkflowState startingState();

	public abstract WorkflowState errorState();

	/**
	 * The retry policy for the workflow
	 * @return
	 */
	public abstract WorkflowRetry retry();

}
