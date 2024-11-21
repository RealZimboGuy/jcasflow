package com.github.realzimboguy.casflow.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import com.github.realzimboguy.casflow.repo.CassandraConnectionPool;
import com.github.realzimboguy.casflow.repo.entity.WorkflowEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@DependsOn("databaseSetup")
public class WorkflowsUnassignedDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowsUnassignedDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}


		public WorkflowEntity save(WorkflowEntity workflowEntity) {

			SimpleStatement statement = new SimpleStatementBuilder(
					"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow " +
							"(bucket, id, status, retries, created, modified, next_activation, started, executor_id, workflow_type, external_id, business_key, state, state_vars) " +
							"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
					.addPositionalValues(
							DaoUtil.getBucket(workflowEntity.getId(), jCasFlowConfig.getDatabaseBucketSize()),
							workflowEntity.getId(),
							workflowEntity.getStatus(),
							workflowEntity.getExecutionCount(),
							workflowEntity.getCreated(),
							workflowEntity.getModified(),
							workflowEntity.getNextActivation(),
							workflowEntity.getStarted(),
							workflowEntity.getExecutorId(),
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

}
