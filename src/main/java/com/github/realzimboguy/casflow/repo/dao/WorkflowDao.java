package com.github.realzimboguy.casflow.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import com.github.realzimboguy.casflow.executor.WorkflowStatus;
import com.github.realzimboguy.casflow.repo.CassandraConnectionPool;
import com.github.realzimboguy.casflow.repo.entity.WorkflowEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class WorkflowDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	public void setState(UUID workflowId, String method) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"UPDATE "+jCasFlowConfig.getDatabaseKeyspace()+".workflow SET state = ? WHERE bucket = ? and id = ?")
				.addPositionalValues(
						method,
						DaoUtil.getBucket(workflowId, jCasFlowConfig.getDatabaseBucketSize()),
						workflowId
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}


		public WorkflowEntity save(WorkflowEntity workflowEntity) {

			SimpleStatement statement = new SimpleStatementBuilder(
					"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow " +
							"(bucket, id, status, execution_count, created, modified, next_activation, started, executor_id,executor_group, workflow_type, external_id, business_key, state, state_vars) " +
							"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
					.addPositionalValues(
							DaoUtil.getBucket(workflowEntity.getId(), jCasFlowConfig.getDatabaseBucketSize()),
							workflowEntity.getId(),
							workflowEntity.getStatus().name(),
							workflowEntity.getExecutionCount(),
							workflowEntity.getCreated(),
							workflowEntity.getModified(),
							workflowEntity.getNextActivation(),
							workflowEntity.getStarted(),
							workflowEntity.getExecutorId(),
							workflowEntity.getExecutorGroup(),
							workflowEntity.getWorkflowType(),
							workflowEntity.getExternalId(),
							workflowEntity.getBusinessKey(),
							workflowEntity.getState(),
							workflowEntity.getStateVars()
					)
					.setConsistencyLevel(consistencyLevel)
					.build();

			ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
			if (rs.wasApplied()) {
				return workflowEntity;
			}
			return null;
		}
	public WorkflowEntity get(UUID id) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT * FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow WHERE bucket = ? and id = ?")
				.addPositionalValues(
						DaoUtil.getBucket(id, jCasFlowConfig.getDatabaseBucketSize()),
						id
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);

		return fromResultSet(rs);
	}

	private WorkflowEntity fromResultSet(ResultSet rs) {

		if (rs == null) {
			return null;
		}
		WorkflowEntity workflowEntity = new WorkflowEntity();
		rs.forEach(row -> {
			if (row != null) {
				workflowEntity.setId(row.getUuid("id"));
				workflowEntity.setStatus(WorkflowStatus.valueOf(row.getString("status")));
				workflowEntity.setExecutionCount(row.getInt("execution_count"));
				workflowEntity.setCreated(row.get("created", Instant.class));
				workflowEntity.setModified(row.get("modified", Instant.class));
				workflowEntity.setNextActivation(row.get("next_activation", Instant.class));
				workflowEntity.setStarted(row.get("started", Instant.class));
				workflowEntity.setExecutorId(row.getString("executor_id"));
				workflowEntity.setExecutorId(row.getString("executor_group"));
				workflowEntity.setWorkflowType(row.getString("workflow_type"));
				workflowEntity.setExternalId(row.getString("external_id"));
				workflowEntity.setBusinessKey(row.getString("business_key"));
				workflowEntity.setState(row.getString("state"));
				workflowEntity.setStateVars(row.getString("state_vars"));
			}
		});
		return workflowEntity;
	}

	public void updateStateVars(UUID id, String json) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"UPDATE "+jCasFlowConfig.getDatabaseKeyspace()+".workflow SET state_vars = ? WHERE bucket = ? and id = ?")
				.addPositionalValues(
						json,
						DaoUtil.getBucket(id, jCasFlowConfig.getDatabaseBucketSize()),
						id
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}

	public void updateStatus(UUID workflowId, WorkflowStatus workflowStatus,int executionCount) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"UPDATE "+jCasFlowConfig.getDatabaseKeyspace()+".workflow SET status = ?, execution_count = ? WHERE bucket = ? and id = ?")
				.addPositionalValues(
						workflowStatus.name(),
						executionCount,
						DaoUtil.getBucket(workflowId, jCasFlowConfig.getDatabaseBucketSize()),
						workflowId
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}
	public void updateStatus(UUID workflowId, WorkflowStatus workflowStatus,int executionCount, String executorId) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"UPDATE "+jCasFlowConfig.getDatabaseKeyspace()+".workflow SET status = ?, execution_count = ?, executor_id = ? WHERE bucket = ? and id = ?")
				.addPositionalValues(
						workflowStatus.name(),
						executionCount,
						executorId,
						DaoUtil.getBucket(workflowId, jCasFlowConfig.getDatabaseBucketSize()),
						workflowId
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}
}
