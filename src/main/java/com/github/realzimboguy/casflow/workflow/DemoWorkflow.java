package com.github.realzimboguy.casflow.workflow;

import com.github.realzimboguy.casflow.executor.Action;
import com.github.realzimboguy.casflow.executor.ExecutorState;
import com.github.realzimboguy.casflow.executor.WorkflowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DemoWorkflow extends JCasWorkFlow {

	Logger logger = LoggerFactory.getLogger(DemoWorkflow.class);

	@Override
	public String getName() {

		return "DemoWorkflow";
	}

	@Override
	public WorkflowState startingState() {

		return DemoWorkflowStates.getVersion;
	}

	@Override
	public WorkflowState errorState() {

		return DemoWorkflowStates.error;
	}

	private enum DemoWorkflowStates implements WorkflowState {
		getVersion(WorkflowStateType.START, "getVersion", "this gets the version"),
		process(WorkflowStateType.NORMAL, "process", "do some processing"),
		process2(WorkflowStateType.NORMAL, "process2", "do some processing"),
		process3(WorkflowStateType.NORMAL, "process3", "do some processing"),
		error(WorkflowStateType.ERROR, "error","this gets the version"),
		done(WorkflowStateType.END, "done","done");

		String            method;
		String            description;
		WorkflowStateType workflowStateType;

		DemoWorkflowStates(WorkflowStateType workflowStateType, String method, String description) {

			this.method = method;
			this.description = description;
			this.workflowStateType = workflowStateType;
		}

		@Override
		public WorkflowStateType stateType() {

			return workflowStateType;
		}

		@Override
		public String method() {

			return method;
		}
	}

	public Action getVersion(ExecutorState executorState) {
		logger.info("Getting version in workflow called");

		executorState.setVar("version", "1.0.0");

		return Action.nextState(DemoWorkflowStates.process);
	}
	public Action process(ExecutorState executorState) {
		logger.info("Processing in workflow called");
		return Action.nextState(DemoWorkflowStates.process2);
	}
	public Action process2(ExecutorState executorState) {
		logger.info("Processing2 in workflow called");
		return Action.nextState(DemoWorkflowStates.process3, Duration.ofSeconds(50));
	}
	public Action process3(ExecutorState executorState) {
		logger.info("Processing3 in workflow called");
		logger.info("Version: " + executorState.getVar("version"));
		return Action.nextState(DemoWorkflowStates.done);
	}
}
