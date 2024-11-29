package com.github.realzimboguy.jcasflow.engine.executor;

import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.dao.*;
import com.github.realzimboguy.jcasflow.engine.repo.entity.ExecutorEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowRunningEntity;
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

	private final WorkflowActionDao workflowActionDao;
	Logger logger = LoggerFactory.getLogger(Dispatcher.class);

	private final WorkflowDao workflowDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;
	private final WorkflowRunningDao workflowRunningDao;
	private final JCasFlowConfig jCasFlowConfig;
	private final ExecutorManager       executorManager;
	private final WorkflowInProgressDao workflowInProgressDao;
	private final ExecutorDao           executorDao;
	private final ApplicationContext    context;

	private String executorId;


	public Dispatcher(WorkflowDao workflowDao, WorkflowNextExecutionDao workflowNextExecutionDao,
	                  WorkflowRunningDao workflowRunningDao,
	                  JCasFlowConfig jCasFlowConfig, ExecutorManager executorManager, WorkflowInProgressDao workflowInProgressDao, ExecutorDao executorDao, ApplicationContext context, WorkflowActionDao workflowActionDao) {

		this.workflowDao = workflowDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
		this.workflowRunningDao = workflowRunningDao;
		this.jCasFlowConfig = jCasFlowConfig;
		this.executorManager = executorManager;
		this.workflowInProgressDao = workflowInProgressDao;
		this.executorDao = executorDao;
		this.context = context;
		this.workflowActionDao = workflowActionDao;
	}

	@PostConstruct
	public void startup() {

		if (!jCasFlowConfig.isExecutorEnabled()) {
		return;
		}

		logger.info("Dispatcher starting");
		this.executorId = UUID.randomUUID().toString();
		//insert the executor id into the database
		ExecutorEntity executorEntity = new ExecutorEntity();
		executorEntity.setId(UUID.fromString(executorId));
		executorEntity.setGroup(jCasFlowConfig.getExecutorGroup());
		executorEntity.setStartedAt(java.time.Instant.now());
		executorEntity.setLastAlive(java.time.Instant.now());
		logger.debug("saving executor: {}", executorEntity);
		executorDao.save(executorEntity);

	}


	@Scheduled(fixedDelayString = "${jcasflow.dispatcher.keep.alive.interval.ms:60000}"  )
	public void executorKeepAlive() {
		if (!jCasFlowConfig.isExecutorEnabled()) {
			return;
		}
		logger.debug("keeping executor alive: {}", executorId);
		executorDao.keepAlive(UUID.fromString(executorId), java.time.Instant.now());
	}

	@Scheduled(fixedDelayString = "${jcasflow.dispatcher.fetch.interval.ms:1000}"  )
	public void getWorkflowsForExecution() {
		if (!jCasFlowConfig.isExecutorEnabled()) {
			return;
		}
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
						workflowActionDao,
						context,
						jCasFlowConfig));

			} else {
				logger.debug("failed to pick up workflow: {}", workflowNextExecutionEntity.getWorkflowId());
			}


		}


	}


}
