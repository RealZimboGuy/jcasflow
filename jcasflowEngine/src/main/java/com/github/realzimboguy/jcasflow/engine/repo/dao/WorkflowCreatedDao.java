package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowCreatedEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowNextExecutionGroupCountEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("databaseSetup")
public class WorkflowCreatedDao {

	ConsistencyLevel consistencyLevel;
	ConsistencyLevel dispatcherConsistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowCreatedDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
		dispatcherConsistencyLevel = jCasFlowConfig.getDispatcherConsistencyLevel();
	}

public List<WorkflowNextExecutionEntity> getWorkflowsForExecution(String group, int size, ZonedDateTime time) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT * FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_next_execution WHERE group = ? and next_execution <= ? LIMIT ?")
				.addPositionalValues(
						group,
						time.toInstant(),
						size
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);

		return fromResultSet(rs);

	}

	private List<WorkflowNextExecutionEntity> fromResultSet(ResultSet rs) {


		if (rs == null) {
			return null;
		}

		List<WorkflowNextExecutionEntity> res = new ArrayList<>();
		rs.forEach(row -> {
			if (row != null) {
				WorkflowNextExecutionEntity workflowNextExecutionEntity = new WorkflowNextExecutionEntity();
				workflowNextExecutionEntity.setGroup(row.getString("group"));
				workflowNextExecutionEntity.setNextExecution( row.get("next_execution", Instant.class));
				workflowNextExecutionEntity.setWorkflowId(row.getUuid("workflow_id"));
				res.add(workflowNextExecutionEntity);
			}
		});
		return res;
	}

	public WorkflowCreatedEntity save(WorkflowCreatedEntity workflowCreatedEntity) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_created " +
						"(group, created, workflow_id, workflow_type, external_id, business_key) VALUES (?,?,?,?,?,?)")
				.addPositionalValues(
						workflowCreatedEntity.getGroup(),
						workflowCreatedEntity.getCreated(),
						workflowCreatedEntity.getWorkflowId(),
						workflowCreatedEntity.getWorkflowType(),
						workflowCreatedEntity.getExternalId(),
						workflowCreatedEntity.getBusinessKey()
				)
				.setConsistencyLevel(dispatcherConsistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		if (rs.wasApplied()) {
			return workflowCreatedEntity;
		}
		return null;
	}

//	public boolean delete(WorkflowNextExecutionEntity workflowNextExecutionEntity) {
//
//		SimpleStatement statement = new SimpleStatementBuilder(
//				"DELETE FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_next_execution WHERE group = ? AND next_execution = ? AND workflow_id = ? IF EXISTS")
//				.addPositionalValues(
//						workflowNextExecutionEntity.getGroup(),
//						workflowNextExecutionEntity.getNextExecution(),
//						workflowNextExecutionEntity.getWorkflowId()
//				)
//				.setConsistencyLevel(dispatcherConsistencyLevel)
//				.build();
//
//		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
//		return rs.wasApplied();
//	}

//	public void deleteAll() {
//
//		SimpleStatement statement = new SimpleStatementBuilder(
//				"TRUNCATE "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_next_execution")
//				.setConsistencyLevel(consistencyLevel)
//				.build();
//
//		CassandraConnectionPool.getSession().execute(statement);
//	}


	public List<WorkflowNextExecutionGroupCountEntity> countNextExecution(String group, Instant start, Instant end) {



		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT group,COUNT(*) as count FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_next_execution" +
						" WHERE group = ? and next_execution >= ? AND next_execution <= ?  group by group ALLOW FILTERING")
				.addPositionalValues(
						group,start,end
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return rs.map(row -> {
			WorkflowNextExecutionGroupCountEntity entity = new WorkflowNextExecutionGroupCountEntity();
			entity.setGroup(row.getString("group"));
			entity.setCount(row.getLong("count"));
			return entity;
		}).all();

	}


	public List<String> getGroups() {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT DISTINCT group FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_next_execution")
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return rs.map(row -> row.getString("group")).all();
	}


	public List<WorkflowNextExecutionGroupCountEntity> countNextExecution(String group, Instant greaterThan) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT group,COUNT(*) as count FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_next_execution" +
						" WHERE group = ? and next_execution >= ?  group by group ")
				.addPositionalValues(
						group,greaterThan
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return rs.map(row -> {
			WorkflowNextExecutionGroupCountEntity entity = new WorkflowNextExecutionGroupCountEntity();
			entity.setGroup(row.getString("group"));
			entity.setCount(row.getLong("count"));
			return entity;
		}).all();

	}




}
