package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowRunningEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class WorkflowRunningDao {

	ConsistencyLevel consistencyLevel;
	ConsistencyLevel dispatcherConsistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowRunningDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}


	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
		dispatcherConsistencyLevel = jCasFlowConfig.getDispatcherConsistencyLevel();
	}



	/**
	 * this is different than delete() as it requires the entity to exist before deleting, it is used to acquire a lock on the record
	 * @param workflowRunningEntity
	 * @return
	 */
	public boolean deleteIfExists(WorkflowRunningEntity workflowRunningEntity) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"DELETE FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_running WHERE workflow_id = ? AND  group = ? IF EXISTS")
				.addPositionalValues(
						workflowRunningEntity.getWorkflowId(),
						workflowRunningEntity.getGroup()
				)
				.setConsistencyLevel(dispatcherConsistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return rs.wasApplied();

	}

	public List<WorkflowRunningEntity> getStuckWorkflows(String executorGroup,int executorRepairStuckWorkflowsMaxSeconds) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT * FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_running WHERE  group = ? AND  started_at < ? ALLOW FILTERING")
				.addPositionalValues(
						executorGroup,
						java.time.Instant.now().minusSeconds(executorRepairStuckWorkflowsMaxSeconds)
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return rs.map(row -> {
			WorkflowRunningEntity workflowRunningEntity = new WorkflowRunningEntity();
			workflowRunningEntity.setGroup(row.getString("group"));
			workflowRunningEntity.setWorkflowId(row.getUuid("workflow_id"));
			workflowRunningEntity.setStartedAt(row.getInstant("started_at"));
			return workflowRunningEntity;
		}).all();


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
