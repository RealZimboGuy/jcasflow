package com.github.realzimboguy.jcasflow.engine.service;

import com.github.realzimboguy.jcasflow.engine.repo.dao.*;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowByTypeEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowCreatedEntity;
import org.springframework.stereotype.Service;

@Service
public class WorkflowBuilderService {

	private final WorkflowDao              workflowDao;
	private final WorkflowNextExecutionDao workflowNextExecutionDao;
	private final WorkflowInProgressDao	workflowInProgressDao;
	private final WorkflowCreatedDao	   workflowCreatedDao;
	private final WorkflowByTypeDao workflowByTypeDao;

	public WorkflowBuilderService(WorkflowDao workflowDao, WorkflowNextExecutionDao workflowNextExecutionDao, WorkflowInProgressDao workflowInProgressDao, WorkflowCreatedDao workflowCreatedDao, WorkflowByTypeDao workflowByTypeDao) {

		this.workflowDao = workflowDao;
		this.workflowNextExecutionDao = workflowNextExecutionDao;
		this.workflowInProgressDao = workflowInProgressDao;
		this.workflowCreatedDao = workflowCreatedDao;
		this.workflowByTypeDao = workflowByTypeDao;
	}

	public WorkflowDao getWorkflowDao() {

		return workflowDao;
	}

	public WorkflowNextExecutionDao getWorkflowNextExecutionDao() {

		return workflowNextExecutionDao;
	}

	public WorkflowInProgressDao getWorkflowInProgressDao() {

		return workflowInProgressDao;
	}

	public WorkflowCreatedDao getWorkflowCreatedDao() {

		return workflowCreatedDao;
	}

	public WorkflowByTypeDao getWorkflowByTypeDao() {

		return workflowByTypeDao;
	}
}

