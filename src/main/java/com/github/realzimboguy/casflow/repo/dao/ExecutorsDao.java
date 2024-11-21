package com.github.realzimboguy.casflow.repo.dao;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import com.github.realzimboguy.casflow.repo.CassandraConnectionPool;
import com.github.realzimboguy.casflow.repo.entity.ExecutorEntity;
import com.github.realzimboguy.casflow.repo.entity.WorkflowInProgressEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@DependsOn("databaseSetup")
public class ExecutorsDao {

	ConsistencyLevel consistencyLevel;

	private final JCasFlowConfig jCasFlowConfig;

	public ExecutorsDao(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		consistencyLevel = jCasFlowConfig.getConsistencyLevel();
	}



	public ExecutorEntity save(ExecutorEntity executor) {

		SimpleStatement statement = new SimpleStatementBuilder(
				"INSERT INTO "+jCasFlowConfig.getDatabaseKeyspace()+".executors " +
						"(id,group,started_at, last_alive) " +
						"VALUES (?,?,?,?)")
				.addPositionalValues(
						executor.getId(),
						executor.getGroup(),
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



}
