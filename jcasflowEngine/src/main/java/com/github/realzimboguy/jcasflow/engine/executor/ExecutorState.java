package com.github.realzimboguy.jcasflow.engine.executor;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowActionDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowActionEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowEntity;
import com.google.gson.Gson;

import java.util.Map;
import java.util.UUID;


public class ExecutorState {

	private final WorkflowDao workflowDao;
	private final WorkflowActionDao workflowActionDao;
	private final UUID workflowId;
	private Gson gson = new Gson();

	public ExecutorState(WorkflowDao workflowDao, WorkflowActionDao workflowActionDao, UUID workflowId) {

		this.workflowDao = workflowDao;
		this.workflowActionDao = workflowActionDao;
		this.workflowId = workflowId;
	}

	public void recordAction(WorkflowActionType actionType,String name, String text) {
		WorkflowEntity workflowEntity = workflowDao.get(workflowId);
		workflowActionDao.save( new WorkflowActionEntity(
				workflowId,
				UUID.randomUUID(),
				workflowEntity.getExecutionCount(),
				workflowEntity.getRetryCount(),
				actionType,
				name,
				text,
				java.time.Instant.now()
		));
	}

	public Object getVar(String varName) {
		WorkflowEntity workflowEntity = workflowDao.get(workflowId);

		if (workflowEntity.getStateVars() == null) {
			return null;
		}
		Map<String, Object> vars = gson.fromJson(workflowEntity.getStateVars(), Map.class);
		return vars.get(varName);

	}

	public <T> T getVar(String varName, Class<T> clazz) {
		Object var = getVar(varName);
		if (var == null) {
			return null;
		}
		return gson.fromJson(gson.toJson(var), clazz);
	}

	public void setVar(String varName, Object varValue) {
		WorkflowEntity workflowEntity = workflowDao.get(workflowId);

		Map<String, Object> vars;
		if (workflowEntity.getStateVars() == null) {
			vars = Map.of(varName, varValue);
		} else {
			vars = gson.fromJson(workflowEntity.getStateVars(), Map.class);
			vars.put(varName, varValue);
		}
		workflowEntity.setStateVars(gson.toJson(vars));
		workflowDao.updateStateVars(workflowEntity.getId(), gson.toJson(vars));
	}

}
