package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowByTypeEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowInProgressEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowInProgressGroupCountEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class WorkflowByTypeDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowByTypeDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}


	public void update(String group,String workflowType, UUID workflowId, String status, String state) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"UPDATE "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_by_type SET status = ?, state = ? " +
						"WHERE group = ? and workflow_type = ? and workflow_id = ?")
				.addPositionalValues(
						status,
						state,
						group,
						workflowType,
						workflowId
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}

	public WorkflowByTypeEntity save(WorkflowByTypeEntity workflowByTypeEntity) {


		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_by_type " +
						"(group,workflow_type,workflow_id,created,external_id,business_key,status,state) " +
						"VALUES (?,?,?,?,?,?,?,?)")
				.addPositionalValues(
						workflowByTypeEntity.getGroup(),
						workflowByTypeEntity.getWorkflowType(),
						workflowByTypeEntity.getWorkflowId(),
						workflowByTypeEntity.getCreated(),
						workflowByTypeEntity.getExternalId(),
						workflowByTypeEntity.getBusinessKey(),
						workflowByTypeEntity.getStatus(),
						workflowByTypeEntity.getState()
				)
				.setConsistencyLevel(consistencyLevel)
				.build();
		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		if (rs.wasApplied()) {
			return workflowByTypeEntity;
		}
		return null;
	}


}
