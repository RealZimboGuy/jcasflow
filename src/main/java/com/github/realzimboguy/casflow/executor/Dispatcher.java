package com.github.realzimboguy.casflow.executor;

import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import com.github.realzimboguy.casflow.repo.dao.*;
import com.github.realzimboguy.casflow.repo.entity.ExecutorEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowNextExecutionEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowRunningEntity;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class Dispatcher {

	 Logger logger = LoggerFactory.getLogger(Dispatcher.class);

	private final WorkflowDao workflowDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;
	private final WorkflowRunningDao workflowRunningDao;
	private final JCasFlowConfig jCasFlowConfig;
	private final ExecutorManager       executorManager;
	private final     WorkflowInProgressDao workflowInProgressDao;
	private final ExecutorsDao executorsDao;
	private final ApplicationContext    context;

	private String executorId;


	public Dispatcher(WorkflowDao workflowDao, WorkflowNextExecutionDao workflowNextExecutionDao,
	                  WorkflowRunningDao workflowRunningDao,
	                  JCasFlowConfig jCasFlowConfig, ExecutorManager executorManager, WorkflowInProgressDao workflowInProgressDao, ExecutorsDao executorsDao, ApplicationContext context) {

		this.workflowDao = workflowDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
		this.workflowRunningDao = workflowRunningDao;
		this.jCasFlowConfig = jCasFlowConfig;
		this.executorManager = executorManager;
		this.workflowInProgressDao = workflowInProgressDao;
		this.executorsDao = executorsDao;
		this.context = context;
	}

	@PostConstruct
	public void startup() {
		this.executorId = UUID.randomUUID().toString();
		//insert the executor id into the database
		ExecutorEntity executorEntity = new ExecutorEntity();
		executorEntity.setId(UUID.fromString(executorId));
		executorEntity.setGroup(jCasFlowConfig.getExecutorGroup());
		executorEntity.setStartedAt(java.time.Instant.now());
		executorEntity.setLastAlive(java.time.Instant.now());
		logger.debug("saving executor: {}", executorEntity);
		executorsDao.save(executorEntity);

	}

	@PostConstruct
	public void dispatch() {
		logger.info("Dispatching...");

		//4137557f-5de1-4a5a-b05a-7df1fa3078e6

		WorkflowEntity workflowEntity = new WorkflowEntity();
		workflowEntity.setId(java.util.UUID.randomUUID());
		workflowEntity.setStatus(WorkflowStatus.NEW);
		workflowEntity.setExecutionCount(0);
		workflowEntity.setExecutorGroup(jCasFlowConfig.getExecutorGroup());
		workflowEntity.setCreated(java.time.ZonedDateTime.now().toInstant());
		workflowEntity.setModified(java.time.ZonedDateTime.now().toInstant());
		workflowEntity.setStarted(java.time.ZonedDateTime.now().toInstant());
		workflowEntity.setWorkflowType("demoWorkflow");
		workflowEntity.setExternalId("external_id");
		workflowEntity.setBusinessKey("business");

		workflowDao.save(workflowEntity);

		WorkflowNextExecutionEntity wf = new WorkflowNextExecutionEntity();
		wf.setGroup(jCasFlowConfig.getExecutorGroup());
		wf.setNextExecution(java.time.ZonedDateTime.now().plusSeconds(5).toInstant());
		wf.setWorkflowId(workflowEntity.getId());

		workflowNextExecutionDao.save(wf);




//		for (int i = 0; i < 1000; i++) {
//			extracted();
//		}

	}

	@Scheduled(fixedDelayString = "${jcasflow.dispatcher.keep.alive.interval.ms:60000}"  )
	public void executorKeepAlive() {
		logger.debug("keeping executor alive: {}", executorId);
		executorsDao.keepAlive(UUID.fromString(executorId), java.time.Instant.now());
	}

	@Scheduled(fixedDelayString = "${jcasflow.dispatcher.fetch.interval.ms:1000}"  )
	public void getWorkflowsForExecution() {

		// get workflows for execution
		List<WorkflowNextExecutionEntity> toExecute =
				workflowNextExecutionDao.getWorkflowsForExecution(jCasFlowConfig.getExecutorGroup(),
						jCasFlowConfig.getDispatcherFetchSize(),
						java.time.ZonedDateTime.now());

		if (toExecute == null || toExecute.isEmpty()) {
			return;
		}

		logger.debug("dispatcher got toExecute: {}", toExecute.size());

		for (WorkflowNextExecutionEntity workflowNextExecutionEntity : toExecute) {

			if (!executorManager.hasFreeThreads()) {
				logger.debug("no free threads, skipping workflow: {}", workflowNextExecutionEntity.getWorkflowId());
				continue;
			}

			// mark the workflow as running
			WorkflowRunningEntity running = new WorkflowRunningEntity();
			running.setGroup(jCasFlowConfig.getExecutorGroup());
			running.setWorkflowId(workflowNextExecutionEntity.getWorkflowId());
			running.setStartedAt(java.time.Instant.now());
			workflowRunningDao.save(running);

			// remove the workflow from the next execution table
			boolean pickedUp = workflowNextExecutionDao.delete(workflowNextExecutionEntity);

			if (pickedUp) {
				logger.debug("picked up workflow: {}", workflowNextExecutionEntity.getWorkflowId());


				executorManager.submit(new WorkflowExecutor(
						jCasFlowConfig.getExecutorGroup(),
						executorId,
						workflowNextExecutionEntity.getWorkflowId(),
						workflowDao,
						workflowInProgressDao,
						workflowNextExecutionDao,
						workflowRunningDao,
						context,
						jCasFlowConfig));

			} else {
				logger.debug("failed to pick up workflow: {}", workflowNextExecutionEntity.getWorkflowId());
			}


		}


	}

	private void extracted() {

		WorkflowEntity wf = new WorkflowEntity();
		wf.setId(java.util.UUID.randomUUID());
		wf.setExecutionCount(0);
		wf.setCreated(java.time.ZonedDateTime.now().toInstant());
		wf.setModified(java.time.ZonedDateTime.now().toInstant());
		wf.setNextActivation(java.time.ZonedDateTime.now().toInstant());
		wf.setStarted(java.time.ZonedDateTime.now().toInstant());
		wf.setExecutorId("executor_id");
		wf.setWorkflowType("workflow_type");
		wf.setExternalId("external_id");
		wf.setBusinessKey("business_key");
		wf.setState("state");
		wf.setStateVars("state_vars");

		wf = workflowDao.save(wf);

		System.out.println("result after saving :" + wf);
	}

}
