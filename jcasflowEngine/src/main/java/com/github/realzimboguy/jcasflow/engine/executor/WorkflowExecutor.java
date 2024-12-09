package com.github.realzimboguy.jcasflow.engine.executor;

import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.dao.*;
import com.github.realzimboguy.jcasflow.engine.repo.entity.*;
import com.github.realzimboguy.jcasflow.engine.workflow.JCasWorkFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
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
	private final WorkflowByTypeDao workflowByTypeDao;
	private final WorkflowInProgressDao workflowInProgressDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;
	private final WorkflowRunningDao workflowRunningDao;
	private final WorkflowActionDao workflowActionDao;
	private final ExecutorState executorState;
	private final     ApplicationContext context;
	private final JCasFlowConfig     jCasFlowConfig;

	public WorkflowExecutor(String group, String executorId, UUID workflowId, WorkflowDao workflowDao, WorkflowByTypeDao workflowByTypeDao, WorkflowInProgressDao workflowInProgressDao,
	                        WorkflowNextExecutionDao workflowNextExecutionDao, WorkflowRunningDao workflowRunningDao, WorkflowActionDao workflowActionDao, ApplicationContext context,
	                        JCasFlowConfig jCasFlowConfig) {
		this.group      = group;
		this.executorId = executorId;
		this.workflowId = workflowId;
		this.workflowDao = workflowDao;
		this.workflowByTypeDao = workflowByTypeDao;
		this.workflowInProgressDao = workflowInProgressDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
		this.workflowRunningDao = workflowRunningDao;
		this.workflowActionDao = workflowActionDao;
		this.executorState = new ExecutorState(workflowDao, workflowActionDao, workflowId);
		this.context = context;
		this.jCasFlowConfig = jCasFlowConfig;
	}

	@Override
	public void run() {

		try {
			logger.info("Executor starting workflow: " + workflowId);

			WorkflowInProgressEntity inProgressEntity =
					new WorkflowInProgressEntity(group,workflowId,java.time.Instant.now());

			workflowInProgressDao.save(inProgressEntity);
			logger.info("Workflow in progress: " + workflowId);


			WorkflowEntity workflowEntity = workflowDao.get(workflowId);
			logger.info("Workflow entity: " + workflowEntity);


			workflowActionDao.save( new WorkflowActionEntity(
					workflowId,
					UUID.randomUUID(),
					workflowEntity.getExecutionCount(),
					workflowEntity.getRetryCount(),
					WorkflowActionType.SYSTEM,
					"Start",
					"Start",
					java.time.Instant.now()
			));


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

					logger.info("Remove in progress record: " + workflowId);
					workflowInProgressDao.delete(inProgressEntity);

					logger.debug("delete workflow running, next state is {} : {}", action.getWorkflowState().method(), workflowId);
					workflowRunningDao.delete(workflowId, group);

					workflowActionDao.save( new WorkflowActionEntity(
							workflowId,
							UUID.randomUUID(),
							workflowEntity.getExecutionCount(),
							workflowEntity.getRetryCount(),
							WorkflowActionType.SYSTEM,
							"Finish",
							"Finish",
							java.time.Instant.now()
					));

					return;
				}

				//schedule for future execution
				//remove in progress flag record

				ZonedDateTime executeAt = action.getExecuteAt();
				if (executeAt == null) {

					executeAt = ZonedDateTime.now().plus(action.getDuration());

				}

				logger.info("Scheduling workflow: " + workflowId + " for execution at: " + executeAt);
				workflowDao.setState(workflowId, action.getWorkflowState().method(),WorkflowStatus.SCHEDULED);

				logger.info("Remove in progress flag: " + workflowId);
				workflowInProgressDao.delete(inProgressEntity);

				workflowNextExecutionDao.save(
						new WorkflowNextExecutionEntity(
						jCasFlowConfig.getExecutorGroup(),
						executeAt.toInstant(),
						workflowEntity.getId()));

				logger.info("Workflow next execution added: " + workflowId);


				workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.SCHEDULED.name(),action.getWorkflowState().method());

				workflowActionDao.save( new WorkflowActionEntity(
						workflowId,
						UUID.randomUUID(),
						workflowEntity.getExecutionCount(),
						workflowEntity.getRetryCount(),
						WorkflowActionType.SYSTEM,
						"Scheduled",
						"Scheduling workflow: " + workflowId + " for execution at: " + executeAt,
						java.time.Instant.now()
				));


			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}catch (Exception e) {
				workflowEntity = workflowDao.get(workflowId);
				if (workflowEntity.getRetryCount()> workflow.retry().getMaxFails()){
					logger.error("Exception in workflow, max retries hit: " + workflowId, e);

					workflowActionDao.save( new WorkflowActionEntity(
							workflowId,
							UUID.randomUUID(),
							workflowEntity.getExecutionCount(),
							workflowEntity.getRetryCount(),
							WorkflowActionType.ERROR,
							"Error",
							e.getMessage(),
							java.time.Instant.now()
					));

					workflowDao.updateStatus(workflowId, WorkflowStatus.FAILED,workflowEntity.getExecutionCount()+1,workflowEntity.getRetryCount() + 1);
					workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.FAILED.name(),workflowEntity.getState());
				}else {
					logger.warn("Exception in workflow, retrying: " + workflowId, e.getCause());

					workflowActionDao.save( new WorkflowActionEntity(
							workflowId,
							UUID.randomUUID(),
							workflowEntity.getExecutionCount(),
							workflowEntity.getRetryCount(),
							WorkflowActionType.SYSTEM,
							"Error",
							throwableToString(e.getCause() == null ? e : e.getCause()),
							java.time.Instant.now()
					));


					workflowDao.updateStatus(workflowId, WorkflowStatus.IN_PROGRESS,workflowEntity.getExecutionCount()+1,workflowEntity.getRetryCount() + 1);
					workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.IN_PROGRESS.name(),workflowEntity.getState());

					// work out how long to wait based on the retry count and the min/max intervals
					long difference = workflow.retry().getMaxInterval().toSeconds() - workflow.retry().getMinInterval().toSeconds();
					long intervalMs = difference / workflow.retry().getMaxFails();
					long nextExecutionInSeconds = workflow.retry().getMinInterval().toSeconds() + (intervalMs * workflowEntity.getRetryCount() + 1);
					ZonedDateTime executeAt = ZonedDateTime.now().plusSeconds(nextExecutionInSeconds);

					logger.info("Scheduling workflow retry : " + workflowId + " execution in seconds:"+ nextExecutionInSeconds+" for execution at: " + executeAt + " retry count: " + workflowEntity.getRetryCount());


					workflowActionDao.save( new WorkflowActionEntity(
							workflowId,
							UUID.randomUUID(),
							workflowEntity.getExecutionCount(),
							workflowEntity.getRetryCount(),
							WorkflowActionType.SYSTEM,
							"Error",
							"Scheduling workflow retry : " + workflowId + " execution in seconds:"+ nextExecutionInSeconds+" for execution at: " + executeAt + " retry count: " + workflowEntity.getRetryCount(),
							java.time.Instant.now()
					));

					workflowNextExecutionDao.save(
							new WorkflowNextExecutionEntity(
									jCasFlowConfig.getExecutorGroup(),
									executeAt.toInstant(),
									workflowEntity.getId()));
				}
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
			workflowDao.updateStatus(workflowId, WorkflowStatus.IN_PROGRESS, workflowEntity.getExecutionCount()+1,0, executorId);
			workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.IN_PROGRESS.name(),methodCall);

		}else {
			logger.info("Workflow status updated to IN_PROGRESS: " + workflowId);
			workflowDao.updateStatus(workflowId, WorkflowStatus.IN_PROGRESS, workflowEntity.getExecutionCount()+1,workflowEntity.getRetryCount(),executorId);
			workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.IN_PROGRESS.name(),methodCall);
		}

		if (
				workflowEntity.getExecutionCount() > jCasFlowConfig.getExecutorMaxExecutionCount()
		) {

			workflowDao.updateStatus(workflowId, WorkflowStatus.FAILED,workflowEntity.getExecutionCount()+1,workflowEntity.getRetryCount());
			logger.info("Workflow status updated to FAILED, max executions hit:{}", workflowId);
			return null;
		}


		method = workflow.getClass().getMethod(methodCall, ExecutorState.class);
		logger.info("Method: " + method);

		workflowActionDao.save( new WorkflowActionEntity(
				workflowId,
				UUID.randomUUID(),
				workflowEntity.getExecutionCount(),
				workflowEntity.getRetryCount(),
				WorkflowActionType.SYSTEM,
				"Execute",
				"Run state: " + methodCall,
				java.time.Instant.now()
		));

		Action action = (Action) method.invoke(workflow,executorState);
		logger.info("Invoked method: " + method);
		logger.info("Action: " + action);

		if (
				action.getWorkflowState().stateType() == WorkflowState.WorkflowStateType.ERROR
		) {

			workflowDao.updateStatus(workflowId, WorkflowStatus.FAILED,workflowEntity.getExecutionCount()+1,workflowEntity.getRetryCount());
			workflowDao.setState(workflowId, action.getWorkflowState().method());
			workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.FAILED.name(),action.getWorkflowState().method());

			logger.info("Workflow status updated to ERROR: " + workflowId);

			workflowActionDao.save( new WorkflowActionEntity(
					workflowId,
					UUID.randomUUID(),
					workflowEntity.getExecutionCount(),
					workflowEntity.getRetryCount(),
					WorkflowActionType.ERROR,
					"Error",
					"Run state: " + methodCall,
					java.time.Instant.now()
			));

			return action;
		}
		if (
				action.getWorkflowState().stateType() == WorkflowState.WorkflowStateType.END
		) {
			workflowDao.updateStatus(workflowId, WorkflowStatus.COMPLETED,workflowEntity.getExecutionCount()+1,workflowEntity.getRetryCount());
			workflowDao.setState(workflowId, action.getWorkflowState().method());
			workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.COMPLETED.name(),action.getWorkflowState().method());

			logger.info("Workflow status updated to COMPLETED: " + workflowId);
			return action;
		}

		if (action.getDuration() == null && action.getExecuteAt() == null) {
			logger.info("Fast execute workflow: " + workflowId);
			workflowDao.setState(workflowId, action.getWorkflowState().method()); // this allows for recovery at this specific state
			workflowByTypeDao.update(workflowEntity.getExecutorGroup(),workflowEntity.getWorkflowType(),workflowId,WorkflowStatus.IN_PROGRESS.name(),action.getWorkflowState().method());

			workflowEntity.setState(action.getWorkflowState().method());
			workflowEntity.setStatus(WorkflowStatus.IN_PROGRESS);
			return runState(workflowEntity, workflow, inProgressEntity);
			//fast execute
		} else {
			return action;

		}

	}

	public static String throwableToString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		return stringWriter.toString();
	}
}
