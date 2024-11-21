package com.github.realzimboguy.casflow.executor;

import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import com.github.realzimboguy.casflow.repo.dao.WorkflowDao;
import com.github.realzimboguy.casflow.repo.dao.WorkflowInProgressDao;
import com.github.realzimboguy.casflow.repo.dao.WorkflowNextExecutionDao;
import com.github.realzimboguy.casflow.repo.dao.WorkflowRunningDao;
import com.github.realzimboguy.casflow.repo.entity.WorkflowEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowInProgressEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowNextExecutionEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowRunningEntity;
import com.github.realzimboguy.casflow.workflow.JCasWorkFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.UUID;

public class WorkflowExecutor implements Runnable {

	Logger logger = LoggerFactory.getLogger(WorkflowExecutor.class);

	private final String group;
	private final String executorId;
	private final UUID   workflowId;
	private final WorkflowDao workflowDao;
	private final WorkflowInProgressDao workflowInProgressDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;
	private final WorkflowRunningDao workflowRunningDao;
	private final ExecutorState executorState;
	private final     ApplicationContext context;
	private final JCasFlowConfig     jCasFlowConfig;

	public WorkflowExecutor(String group, String executorId, UUID workflowId, WorkflowDao workflowDao, WorkflowInProgressDao workflowInProgressDao,
	                        WorkflowNextExecutionDao workflowNextExecutionDao, WorkflowRunningDao workflowRunningDao,ApplicationContext context,
	                        JCasFlowConfig jCasFlowConfig) {
		this.group      = group;
		this.executorId = executorId;
		this.workflowId = workflowId;
		this.workflowDao = workflowDao;
		this.workflowInProgressDao = workflowInProgressDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
		this.workflowRunningDao = workflowRunningDao;
		this.executorState = new ExecutorState(workflowDao, workflowId);
		this.context = context;
		this.jCasFlowConfig = jCasFlowConfig;
	}

	@Override
	public void run() {

		try {
			logger.info("Executor starting workflow: " + workflowId);

			WorkflowInProgressEntity inProgressEntity = new WorkflowInProgressEntity();
			inProgressEntity.setGroup(group);
			inProgressEntity.setWorkflowId(workflowId);
			inProgressEntity.setStartedAt(java.time.Instant.now());

			workflowInProgressDao.save(inProgressEntity);
			logger.info("Workflow in progress: " + workflowId);

			WorkflowEntity workflowEntity = workflowDao.get(workflowId);
			logger.info("Workflow entity: " + workflowEntity);


			JCasWorkFlow workflow = (JCasWorkFlow) context.getBean(workflowEntity.getWorkflowType());
			// Load the class dynamically
			Class<?> exampleClass = workflow.getClass();

			//iterate and print all methods on the class
//			for (int i = 0; i < workflow.getClass().getMethods().length; i++) {
//				logger.info(workflow.getClass().getMethods()[i].getName());
//			}

			Method method = null;
			try {
				Action action = runState(workflowEntity, workflow, inProgressEntity);

				if (action == null){
					logger.warn("Action is null from runState, workflow: " + workflowId );
					return;
				}

				if (action.getWorkflowState().stateType()==WorkflowState.WorkflowStateType.END
				|| action.getWorkflowState().stateType()==WorkflowState.WorkflowStateType.MANUAL
				|| action.getWorkflowState().stateType()==WorkflowState.WorkflowStateType.ERROR
				){

					logger.debug("delete workflow running, next state is {} : {}", action.getWorkflowState().method(), workflowId);
					workflowRunningDao.delete(workflowId, group);
					logger.info("Remove in progress record: " + workflowId);
					workflowInProgressDao.delete(inProgressEntity);
					return;
				}

				//schedule for future execution
				//remove in progress flag record

				ZonedDateTime executeAt = action.getExecuteAt();
				if (executeAt == null) {

					executeAt = ZonedDateTime.now().plus(action.getDuration());

				}

				logger.info("Scheduling workflow: " + workflowId + " for execution at: " + executeAt);
				workflowDao.setState(workflowId, action.getWorkflowState().method());

				logger.info("Remove in progress flag: " + workflowId);
				workflowInProgressDao.delete(inProgressEntity);

				WorkflowNextExecutionEntity wf = new WorkflowNextExecutionEntity();
				wf.setGroup(jCasFlowConfig.getExecutorGroup());
				wf.setNextExecution(executeAt.toInstant());
				wf.setWorkflowId(workflowEntity.getId());

				workflowNextExecutionDao.save(wf);
				logger.info("Workflow next execution added: " + workflowId);

			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			// Invoke the method on the instance

			logger.debug("delete workflow running: " + workflowId);
			workflowRunningDao.delete(workflowId, group);


		}catch (Exception e) {
			logger.error("Error executing workflow: " + workflowId, e);
		}




	}

	private Action runState(WorkflowEntity workflowEntity, JCasWorkFlow workflow, WorkflowInProgressEntity inProgressEntity) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		Method method;
		String methodCall = workflowEntity.getState();
		if (methodCall == null || workflowEntity.getStatus() == WorkflowStatus.NEW) {
			methodCall = workflow.startingState().method();
			logger.info("Starting state :{}", methodCall);
			logger.info("Workflow status updated to IN_PROGRESS from NEW: " + workflowId);
			workflowDao.updateStatus(workflowId, WorkflowStatus.IN_PROGRESS, workflowEntity.getExecutionCount()+1, executorId);

		}else {
			logger.info("Workflow status updated to IN_PROGRESS: " + workflowId);
			workflowDao.updateStatus(workflowId, WorkflowStatus.IN_PROGRESS, workflowEntity.getExecutionCount()+1,executorId);
		}

		if (
				workflowEntity.getExecutionCount() > jCasFlowConfig.getExecutorMaxExecutionCount()
		) {

			workflowDao.updateStatus(workflowId, WorkflowStatus.FAILED,workflowEntity.getExecutionCount()+1);
			logger.info("Workflow status updated to FAILED, max retries hit:{}", workflowId);
			return null;
		}


		method = workflow.getClass().getMethod(methodCall, ExecutorState.class);
		logger.info("Method: " + method);

		Action action = (Action) method.invoke(workflow,executorState);
		logger.info("Invoked method: " + method);
		logger.info("Action: " + action);

		if (
				action.getWorkflowState().stateType() == WorkflowState.WorkflowStateType.ERROR
		) {

			workflowDao.updateStatus(workflowId, WorkflowStatus.FAILED,workflowEntity.getExecutionCount()+1);
			workflowDao.setState(workflowId, action.getWorkflowState().method());
			logger.info("Workflow status updated to ERROR: " + workflowId);
			return action;
		}
		if (
				action.getWorkflowState().stateType() == WorkflowState.WorkflowStateType.END
		) {
			workflowDao.updateStatus(workflowId, WorkflowStatus.COMPLETED,workflowEntity.getExecutionCount()+1);
			workflowDao.setState(workflowId, action.getWorkflowState().method());
			logger.info("Workflow status updated to COMPLETED: " + workflowId);
			return action;
		}

		if (action.getDuration() == null && action.getExecuteAt() == null) {
			logger.info("Fast execute workflow: " + workflowId);
			workflowDao.setState(workflowId, action.getWorkflowState().method()); // this allows for recovery at this specific state

			workflowEntity.setState(action.getWorkflowState().method());
			workflowEntity.setStatus(WorkflowStatus.IN_PROGRESS);
			return runState(workflowEntity, workflow, inProgressEntity);
			//fast execute
		} else {
			return action;

		}

	}

}
