package com.github.realzimboguy.jcasflow.engine.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.repo.CassandraConnectionPool;
import com.github.realzimboguy.jcasflow.engine.repo.entity.ExecutorEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowDefinitionEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class ExecutorDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public ExecutorDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}



	public ExecutorEntity save(ExecutorEntity executor) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".executors " +
						"(id,group,host,started_at, last_alive) " +
						"VALUES (?,?,?,?,?)")
				.addPositionalValues(
						executor.getId(),
						executor.getGroup(),
						executor.getHost(),
						executor.getStartedAt(),
						executor.getLastAlive()
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		if (rs.wasApplied()) {
			return executor;
		}
		return null;
	}

	public void keepAlive(UUID id, Instant lastAlive) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"UPDATE "+jCasFlowConfig.getDatabaseKeyspace()+".executors SET last_alive = ? WHERE id = ?")
				.addPositionalValues(
						lastAlive,
						id
				)
				.setConsistencyLevel(consistencyLevel)
				.build();

		CassandraConnectionPool.getSession().execute(statement);

	}

	public List<ExecutorEntity> getAll () {

		SimpleStatement statement = new SimpleStatementBuilder(
				"SELECT * FROM "+jCasFlowConfig.getDatabaseKeyspace()+".executors")
				.setConsistencyLevel(consistencyLevel)
				.build();

		ResultSet rs = CassandraConnectionPool.getSession().execute(statement);
		return fromResultSetList(rs);
	}

	private List<ExecutorEntity> fromResultSetList(ResultSet rs) {

		if (rs == null || !rs.iterator().hasNext()) {
			// Return null if the ResultSet is null or has no rows
			return null;
		}

		List<ExecutorEntity> executorEntities = new ArrayList<>();
		rs.forEach(row -> {
			if (row != null) {
				ExecutorEntity executorEntity = new ExecutorEntity();
				executorEntity.setId(row.getUuid("id"));
				executorEntity.setGroup(row.getString("group"));
				executorEntity.setHost(row.getString("host"));
				executorEntity.setStartedAt(row.getInstant("started_at"));
				executorEntity.setLastAlive(row.getInstant("last_alive"));
				executorEntities.add(executorEntity);
			}
		});
		return executorEntities;
	}



}
