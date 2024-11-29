package com.github.realzimboguy.jcasflow.engine.executor;

import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowNextExecutionDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowRunningDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowRunningEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairManager {


	Logger logger = LoggerFactory.getLogger(Dispatcher.class);

	private final WorkflowRunningDao       workflowRunningDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;
	private final JCasFlowConfig           jCasFlowConfig;


	public RepairManager(WorkflowRunningDao workflowRunningDao, WorkflowNextExecutionDao workflowNextExecutionDao, JCasFlowConfig jCasFlowConfig) {

		this.workflowRunningDao = workflowRunningDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
		this.jCasFlowConfig = jCasFlowConfig;
	}

	@Scheduled(initialDelay = 10000,
			fixedDelayString = "${jcasflow.dispatcher.repair.interval.ms:120000}")
	public void getStuckWorkflows() {

		List<WorkflowRunningEntity> toFix =
				workflowRunningDao.getStuckWorkflows(jCasFlowConfig.getExecutorGroup(),jCasFlowConfig.getExecutorRepairStuckWorkflowsMaxSeconds());


		for (WorkflowRunningEntity workflowRunningEntity : toFix) {
			// remove the workflow from the next execution table
			boolean pickedUp = workflowRunningDao.deleteIfExists(workflowRunningEntity);
			logger.info("Workflow stuck: " + workflowRunningEntity.getWorkflowId());

			if (pickedUp) {
				logger.info("re-scheduling stuck workflow: " + workflowRunningEntity.getWorkflowId());
				// add the workflow back to the next execution table
				WorkflowNextExecutionEntity wf = new WorkflowNextExecutionEntity();
				wf.setGroup(jCasFlowConfig.getExecutorGroup());
				wf.setNextExecution(java.time.ZonedDateTime.now().plusSeconds(5).toInstant());
				wf.setWorkflowId(workflowRunningEntity.getWorkflowId());

				workflowNextExecutionDao.save(wf);
				logger.info("stuck workflow scheduled again : " + workflowRunningEntity.getWorkflowId());

			}
		}


	}

}
