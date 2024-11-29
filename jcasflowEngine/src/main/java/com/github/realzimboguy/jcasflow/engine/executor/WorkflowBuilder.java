package com.github.realzimboguy.jcasflow.engine.executor;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowNextExecutionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionEntity;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class WorkflowBuilder {

	Logger         logger         = LoggerFactory.getLogger(WorkflowBuilder.class);
	WorkflowEntity workflowEntity = new WorkflowEntity();
	WorkflowNextExecutionEntity nextExecutionEntity = new WorkflowNextExecutionEntity();

	Gson gson = new Gson();
	private boolean buildCalled = false;

	public static WorkflowBuilder builder() {
		return new WorkflowBuilder();
	}

	public WorkflowBuilder build() {

		logger.info("Building workflow");
		if (workflowEntity.getId() == null) {
			workflowEntity.setId(UUID.randomUUID());
		}
		workflowEntity.setStatus(WorkflowStatus.NEW);
		workflowEntity.setExecutionCount(0);
		workflowEntity.setRetryCount(0);
		workflowEntity.setCreated(java.time.ZonedDateTime.now().toInstant());
		workflowEntity.setModified(java.time.ZonedDateTime.now().toInstant());
		workflowEntity.setStarted(java.time.ZonedDateTime.now().toInstant());


		nextExecutionEntity.setGroup(workflowEntity.getExecutorGroup());
		nextExecutionEntity.setWorkflowId(workflowEntity.getId());

		if (nextExecutionEntity.getNextExecution() == null){
			nextExecutionEntity.setNextExecution(java.time.ZonedDateTime.now().toInstant());
		}

		buildCalled = true;

		return this;
	}

	public void save(WorkflowDao workflowDao, WorkflowNextExecutionDao workflowNextExecutionDao) {

		if (!buildCalled){
			logger.warn("build() was not called before save()");
			throw new RuntimeException("build() must be called before save()");
		}
		workflowDao.save(workflowEntity);
		workflowNextExecutionDao.save(nextExecutionEntity);
		logger.info("Workflow saved");

	}

	public WorkflowBuilder withBusinessKey(String business) {

		workflowEntity.setBusinessKey(business);
		return this;
	}


	public WorkflowBuilder withExecutorGroup(String executorGroup) {

		workflowEntity.setExecutorGroup(executorGroup);
		return this;
	}

	public WorkflowBuilder withExternalId(String externalId) {

		workflowEntity.setExternalId(externalId);
		return this;
	}

	public WorkflowBuilder withId(UUID uuid) {

		this.workflowEntity.setId(uuid);
		return this;
	}

	public WorkflowBuilder withNextExecution(Instant instant) {

		nextExecutionEntity.setNextExecution(instant);
		return this;
	}

	public WorkflowBuilder withStateVar(String varName, String varValue) {

		Map<String, Object> vars;
		if (workflowEntity.getStateVars() == null) {
			vars = Map.of(varName, varValue);
		} else {
			vars = gson.fromJson(workflowEntity.getStateVars(), Map.class);
			vars.put(varName, varValue);
		}
		workflowEntity.setStateVars(gson.toJson(vars));
		return this;
	}

	public WorkflowBuilder withWorkflowType(String workflowType) {

		workflowEntity.setWorkflowType(workflowType);
		return this;
	}
}
