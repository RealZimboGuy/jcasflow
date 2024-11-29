package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowActionType;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowActionEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@DependsOn("databaseSetup")
public class WorkflowActionDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public WorkflowActionDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}


	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}


		public WorkflowActionEntity save(WorkflowActionEntity workflowActionEntity) {


			SimpleStatement statement = new SimpleStatementBuilder(
					"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".workflow_actions " +
							"(bucket, id, workflow_id, execution_count,retry_count, type, name, text, date_time) " +
							"VALUES (?,?,?,?,?,?,?,?,?)")
					.addPositionalValues(
							DaoUtil.getBucket(workflowActionEntity.getId(), jCasFlowConfig.getDatabaseBucketSize()),
							workflowActionEntity.getId(),
							workflowActionEntity.getWorkflowId(),
							workflowActionEntity.getExecutionCount(),
							workflowActionEntity.getRetryCount(),
							workflowActionEntity.getType().name(),
							workflowActionEntity.getName(),
							workflowActionEntity.getText(),
							workflowActionEntity.getDate_time()
					)
					.setConsistencyLevel(consistencyLevel)
					.build();


			ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
			if (rs.wasApplied()) {
				return workflowActionEntity;
			}
			return null;

		}

	private WorkflowActionEntity fromResultSet(ResultSet rs) {

		if (rs == null) {
			return null;
		}
		WorkflowActionEntity workflowActionEntity = new WorkflowActionEntity();
		rs.forEach(row -> {
			if (row != null) {
				workflowActionEntity.setId(row.getUuid("id"));
				workflowActionEntity.setWorkflowId(row.getUuid("workflow_id"));
				workflowActionEntity.setExecutionCount(row.getInt("execution_count"));
				workflowActionEntity.setRetryCount(row.getInt("retry_count"));
				workflowActionEntity.setType(WorkflowActionType.valueOf(row.getString("type")));
				workflowActionEntity.setName(row.getString("name"));
				workflowActionEntity.setText(row.getString("text"));
				workflowActionEntity.setDate_time(row.getInstant("date_time"));
			}
		});
		return workflowActionEntity;
	}

}
