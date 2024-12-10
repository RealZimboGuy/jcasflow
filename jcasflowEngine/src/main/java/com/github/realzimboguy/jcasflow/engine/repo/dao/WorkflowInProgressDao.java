package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowInProgressEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowInProgressGroupCountEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@DependsOn("databaseSetup")
public class WorkflowInProgressDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowInProgressDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}



	public WorkflowInProgressEntity save(WorkflowInProgressEntity workflowInProgressEntity) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_in_progress " +
						"(group,workflow_id, started_at) " +
						"VALUES (?,?,?)")
				.addPositionalValues(
						workflowInProgressEntity.getGroup(),
						workflowInProgressEntity.getWorkflowId(),
						workflowInProgressEntity.getStartedAt()
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		if (rs.wasApplied()) {
			return workflowInProgressEntity;
		}
		return null;
	}

	public void delete(WorkflowInProgressEntity workflowInProgressEntity) {


		SimpleStatement statement = new SimpleStatementBuilder(
				"DELETE FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_in_progress WHERE group = ? AND  workflow_id = ?")
				.addPositionalValues(
						workflowInProgressEntity.getGroup(),
						workflowInProgressEntity.getWorkflowId()
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}

	public List<WorkflowInProgressGroupCountEntity> countInProgress(String group) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT group,COUNT(*) as count FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_in_progress WHERE group = ? group by group")
				.addPositionalValues(
						group
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return rs.map(row -> {
			WorkflowInProgressGroupCountEntity entity = new WorkflowInProgressGroupCountEntity();
			entity.setGroup(row.getString("group"));
			entity.setCount(row.getLong("count"));
			return entity;
		}).all();

	}


}
