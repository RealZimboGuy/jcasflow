package com.github.realzimboguy.jcasflow.engine.repo;

import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateIndex;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@DependsOn("cassandraConnectionPool")
public class DatabaseSetup {

	private final JCasFlowConfig JCasFlowConfig;

	public DatabaseSetup(JCasFlowConfig JCasFlowConfig) {

		this.JCasFlowConfig = JCasFlowConfig;
	}

	@PostConstruct
	public void setup() {

			CreateKeyspace createKeyspace = SchemaBuilder.createKeyspace(JCasFlowConfig.getDatabaseKeyspace())
					.ifNotExists()
					.withSimpleStrategy(JCasFlowConfig.getDatabaseReplicas());

			CassandraConnectionPool.getSession().execute(createKeyspace.build());


		// Create workflow table
		CreateTable createWorkflowTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow")
				.ifNotExists()
				.withPartitionKey("bucket", DataTypes.TEXT)
				.withClusteringColumn("id", DataTypes.UUID)
				.withColumn("status", DataTypes.TEXT)
				.withColumn("execution_count", DataTypes.INT)
				.withColumn("retry_count", DataTypes.INT)
				.withColumn("created", DataTypes.TIMESTAMP)
				.withColumn("modified", DataTypes.TIMESTAMP)
				.withColumn("next_activation", DataTypes.TIMESTAMP)
				.withColumn("started", DataTypes.TIMESTAMP)
				.withColumn("executor_id", DataTypes.TEXT)
				.withColumn("executor_group", DataTypes.TEXT)
				.withColumn("workflow_type", DataTypes.TEXT)
				.withColumn("external_id", DataTypes.TEXT)
				.withColumn("business_key", DataTypes.TEXT)
				.withColumn("state", DataTypes.TEXT)
				.withColumn("state_vars", DataTypes.TEXT);
		CassandraConnectionPool.getSession().execute(createWorkflowTable.build());

		// Create workflow_actions table
		CreateTable createWorkflowActionsTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow_actions")
				.ifNotExists()
				.withPartitionKey("bucket", DataTypes.TEXT)
				.withPartitionKey("workflow_id", DataTypes.UUID)
				.withClusteringColumn("id", DataTypes.UUID)
				.withColumn("execution_count", DataTypes.INT)
				.withColumn("retry_count", DataTypes.INT)
				.withColumn("type", DataTypes.TEXT)
				.withColumn("name", DataTypes.TEXT)
				.withColumn("text", DataTypes.TEXT)
				.withColumn("date_time", DataTypes.TIMESTAMP);

		CassandraConnectionPool.getSession().execute(createWorkflowActionsTable.build());

		// Create unassigned workflows table
		//CREATE TABLE workflows (
		//    day_bucket DATE,              -- Partition key
		//    next_execution TIMESTAMP,     -- Clustering key
		//    workflow_id UUID,             -- Clustering key
		//    PRIMARY KEY ((day_bucket), next_execution, workflow_id)
		//);
		CreateTable createWorkflowsUnassignedTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow_next_execution")
				.ifNotExists()
				.withPartitionKey("group", DataTypes.TEXT)
				.withClusteringColumn("next_execution", DataTypes.TIMESTAMP)
				.withClusteringColumn("workflow_id", DataTypes.UUID);
		CassandraConnectionPool.getSession().execute(createWorkflowsUnassignedTable.build());

		CreateTable createWorkflowsRunningTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow_running")
				.ifNotExists()
				.withPartitionKey("group", DataTypes.TEXT)
				.withClusteringColumn("workflow_id", DataTypes.UUID)
				.withColumn("started_at", DataTypes.TIMESTAMP);
		CassandraConnectionPool.getSession().execute(createWorkflowsRunningTable.build());

		CreateIndex createIndex = SchemaBuilder.createIndex("workflow_running_started_at_index")
				.ifNotExists()
				.onTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow_running")
				.andColumn("started_at");
		CassandraConnectionPool.getSession().execute(createIndex.build());


		CreateTable createWorkflowsInProgressTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow_in_progress")
				.ifNotExists()
				.withPartitionKey("group", DataTypes.TEXT)
				.withClusteringColumn("workflow_id", DataTypes.UUID)
				.withColumn ("started_at", DataTypes.TIMESTAMP);

		CassandraConnectionPool.getSession().execute(createWorkflowsInProgressTable.build());

		CreateTable createWorkflowDefinitionsTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "workflow_definitions")
				.ifNotExists()
				.withPartitionKey("name", DataTypes.TEXT)
				.withColumn ("created", DataTypes.TIMESTAMP)
				.withColumn ("updated", DataTypes.TIMESTAMP)
				.withColumn ("flow_chart", DataTypes.TEXT);

		CassandraConnectionPool.getSession().execute(createWorkflowDefinitionsTable.build());


		// Create executors table
		CreateTable createExecutorsTable = SchemaBuilder.createTable(JCasFlowConfig.getDatabaseKeyspace(), "executors")
				.ifNotExists()
				.withPartitionKey("id", DataTypes.UUID)
				.withColumn("group", DataTypes.TEXT)
				.withColumn("started_at", DataTypes.TIMESTAMP)
				.withColumn("last_alive", DataTypes.TIMESTAMP);

		CassandraConnectionPool.getSession().execute(createExecutorsTable.build());



	}

}
