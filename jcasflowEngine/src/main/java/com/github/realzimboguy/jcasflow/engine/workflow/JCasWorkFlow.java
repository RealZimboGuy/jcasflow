package com.github.realzimboguy.jcasflow.engine.workflow;

import com.github.realzimboguy.jcasflow.engine.executor.WorkflowRetry;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowState;

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
