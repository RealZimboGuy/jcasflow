package com.github.realzimboguy.casflow.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import com.github.realzimboguy.casflow.repo.CassandraConnectionPool;
import com.github.realzimboguy.casflow.repo.entity.WorkflowRunningEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class WorkflowRunningDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowRunningDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}



	public WorkflowRunningEntity save(WorkflowRunningEntity workflowRunningEntity) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_running " +
						"(group,workflow_id, started_at) " +
						"VALUES (?,?,?)")
				.addPositionalValues(
						workflowRunningEntity.getGroup(),
						workflowRunningEntity.getWorkflowId(),
						workflowRunningEntity.getStartedAt()
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		if (rs.wasApplied()) {
			return workflowRunningEntity;
		}
		return null;
	}

	public void delete(UUID workflowId, String group) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"DELETE FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_running WHERE workflow_id = ? AND  group = ?  ")
				.addPositionalValues(
						workflowId,
						group
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);
	}

	public void deleteAll() {

		SimpleStatement statement = new SimpleStatementBuilder(
				"TRUNCATE workflow_next_execution")
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);
	}


}
