package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowActionType;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.ExecutorEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowActionEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowDefinitionEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowRunningEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class WorkflowDefinitionDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowDefinitionDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}



	public WorkflowDefinitionEntity save(WorkflowDefinitionEntity workflowDefinitionEntity) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_definitions " +
						"(name, created, updated, flow_chart) " +
						"VALUES (?,?,?,?)")
				.addPositionalValues(
						workflowDefinitionEntity.getName(),
						workflowDefinitionEntity.getCreated(),
						workflowDefinitionEntity.getUpdated(),
						workflowDefinitionEntity.getFlowChart()
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		if (rs.wasApplied()) {
			return workflowDefinitionEntity;
		}
		return null;
	}


	public List<WorkflowDefinitionEntity> getAll () {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT * FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_definitions")
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return fromResultSetList(rs);
	}

	public WorkflowDefinitionEntity get(String name) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT * FROM "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_definitions WHERE name = ?")
				.addPositionalValues(name)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return fromResultSet(rs);
	}


	private WorkflowDefinitionEntity fromResultSet(ResultSet rs) {

		if (rs == null || !rs.iterator().hasNext()) {
			// Return null if the ResultSet is null or has no rows
			return null;
		}

		WorkflowDefinitionEntity workflowDefinitionEntity = new WorkflowDefinitionEntity();
		rs.forEach(row -> {
			if (row != null) {
				workflowDefinitionEntity.setName(row.getString("name"));
				workflowDefinitionEntity.setCreated(row.getInstant("created"));
				workflowDefinitionEntity.setUpdated(row.getInstant("updated"));
				workflowDefinitionEntity.setFlowChart(row.getString("flow_chart"));
			}
		});
		return workflowDefinitionEntity;
	}

	private List<WorkflowDefinitionEntity> fromResultSetList(ResultSet rs) {

		if (rs == null || !rs.iterator().hasNext()) {
			// Return null if the ResultSet is null or has no rows
			return null;
		}

		List<WorkflowDefinitionEntity> workflowDefinitionEntities = new ArrayList<>();
		rs.forEach(row -> {
			if (row != null) {
				WorkflowDefinitionEntity workflowDefinitionEntity = new WorkflowDefinitionEntity();
				workflowDefinitionEntity.setName(row.getString("name"));
				workflowDefinitionEntity.setCreated(row.getInstant("created"));
				workflowDefinitionEntity.setUpdated(row.getInstant("updated"));
				workflowDefinitionEntity.setFlowChart(row.getString("flow_chart"));
				workflowDefinitionEntities.add(workflowDefinitionEntity);
			}
		});
		return workflowDefinitionEntities;
	}


}
