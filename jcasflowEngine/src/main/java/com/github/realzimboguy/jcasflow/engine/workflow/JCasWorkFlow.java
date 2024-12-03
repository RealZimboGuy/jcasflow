package com.github.realzimboguy.jcasflow.engine.workflow;

import com.github.realzimboguy.jcasflow.engine.executor.WorkflowAllowedTransition;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowRetry;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class JCasWorkFlow {

	List<WorkflowAllowedTransition> permittedTransitions = new ArrayList<>();


	public abstract String getName();

	public abstract String getDescription();

	public abstract WorkflowState startingState();

	public abstract WorkflowState errorState();

	public abstract List<WorkflowAllowedTransition> permittedTransitions();

	/**
	 * The retry policy for the workflow
	 * @return
	 */
	public abstract WorkflowRetry retry();

}
