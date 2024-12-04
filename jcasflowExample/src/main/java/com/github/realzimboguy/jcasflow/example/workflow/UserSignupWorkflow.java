package com.github.realzimboguy.jcasflow.example.workflow;

import com.github.realzimboguy.jcasflow.engine.executor.*;
import com.github.realzimboguy.jcasflow.engine.workflow.JCasWorkFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class UserSignupWorkflow extends JCasWorkFlow {

	Logger logger = LoggerFactory.getLogger(UserSignupWorkflow.class);

	@Override
	public String getName() {

		return "UserSignupWorkflow";
	}

	@Override
	public String getDescription() {

		return "";
	}

	@Override
	public WorkflowState startingState() {
		return DemoWorkflowStates.getVersion;
	}

	@Override
	public WorkflowState errorState() {
		return DemoWorkflowStates.error;
	}

	@Override
	public List<WorkflowAllowedTransition> permittedTransitions() {

		return List.of(
			new WorkflowAllowedTransition(DemoWorkflowStates.getVersion, DemoWorkflowStates.process),
			new WorkflowAllowedTransition(DemoWorkflowStates.process, DemoWorkflowStates.process2),
			new WorkflowAllowedTransition(DemoWorkflowStates.process, DemoWorkflowStates.process3),
			new WorkflowAllowedTransition(DemoWorkflowStates.process2, DemoWorkflowStates.process3),
			new WorkflowAllowedTransition(DemoWorkflowStates.process3, DemoWorkflowStates.done)
		);
	}


	@Override
	public WorkflowRetry retry() {
		return new WorkflowRetry(20,Duration.ofSeconds(15), Duration.ofMinutes(2));
	}

	private enum DemoWorkflowStates implements WorkflowState {
		getVersion(WorkflowStateType.START, "getVersion", "this gets the version"),
		process(WorkflowStateType.NORMAL, "process", "do some processing"),
		process2(WorkflowStateType.NORMAL, "process2", "do some processing"),
		process3(WorkflowStateType.NORMAL, "process3", "do some processing"),
		error(WorkflowStateType.ERROR, "error","an error has occurred"),
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
		@Override
		public String description() {

			return description;
		}



	}

	public Action getVersion(ExecutorState executorState) {
		logger.info("Getting version in workflow called");

		executorState.setVar("version", "1.0.0");
		executorState.setVar("exceptionCounter", 1);

		return Action.nextState(DemoWorkflowStates.process);
	}
	public Action process(ExecutorState executorState) {
		logger.info("Processing in workflow called");

		executorState.recordAction(WorkflowActionType.USER, "process", "Processing in workflow called");

		MyObject myObject = new MyObject();
		myObject.setName("John");
		myObject.setAge(30);

		executorState.setVar("version", "1.0.0");
		executorState.setVar("myObject", myObject);
		return Action.nextState(DemoWorkflowStates.process2);
	}
	public Action process2(ExecutorState executorState) {
		int exceptionCounter = executorState.getVar("exceptionCounter", Integer.class);
		logger.info("Processing2 in workflow called: " + exceptionCounter);
		if (exceptionCounter < 5) {
			executorState.setVar("exceptionCounter", exceptionCounter + 1);
			throw new RuntimeException("Test exception: + " + exceptionCounter);
		}else {
			logger.info("No exception");

		}

		return Action.nextState(DemoWorkflowStates.process3, Duration.ofHours(1));
	}
	public Action process3(ExecutorState executorState) {
		logger.info("Processing3 in workflow called");
		logger.info("Version: " + executorState.getVar("version",String.class));

		MyObject myObject =  executorState.getVar("myObject", MyObject.class);
		logger.info("MyObject: " + myObject.getName() + " " + myObject.getAge());

		return Action.nextState(DemoWorkflowStates.done);
	}

	private class MyObject {
		private String name;
		private int age;

		public String getName() {

			return name;
		}

		public void setName(String name) {

			this.name = name;
		}

		public int getAge() {

			return age;
		}

		public void setAge(int age) {

			this.age = age;
		}
	}
}
