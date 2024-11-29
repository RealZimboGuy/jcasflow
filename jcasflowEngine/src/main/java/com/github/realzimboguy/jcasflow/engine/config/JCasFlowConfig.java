package com.github.realzimboguy.jcasflow.engine.config;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:jcasflowengine.properties")
@PropertySource("classpath:application.properties")
public class JCasFlowConfig {

	@Value("${app.version}")
	private String appVersion;
	@Value("${jcasflow.database.keyspace:casflow}")
	private String databaseKeyspace;
	@Value("${jcasflow.database.ip:localhost}")
	private String databaseIp;
	@Value("${jcasflow.database.port:9042}")
	private int databasePort;
	@Value("${jcasflow.database.replicas:1}")
	private int databaseReplicas;
	@Value("${jcasflow.database.dc.name:datacenter1}")
	private String databaseDcName;
	@Value("${jcasflow.database.consistencyLevel:ONE}")
	private ConsistencyLevel consistencyLevel;
	//this should always be a quorum based consistency level for production
	@Value("${jcasflow.database.dispatcher.consistencyLevel:ONE}")
	private ConsistencyLevel dispatcherConsistencyLevel;
	@Value("${jcasflow.database.bucket.size:1}")
	private int databaseBucketSize;
	@Value("${jcasflow.dispatcher.fetch.size:50}")
	private int dispatcherFetchSize;
	@Value("${jcasflow.executor.enabled:true}")
	private boolean executorEnabled;
	@Value("${jcasflow.executor.group:jcasflow}")
	private String executorGroup;
	@Value("${jcasflow.executor.max.execution.count:1000}")
	private int    executorMaxExecutionCount;
	@Value("${jcasflow.executor.thread.pool.size:10}")
	private int    executorThreadPoolSize;
	@Value("${jcasflow.executor.repair.stuck.workflows.max.seconds:300}")
	private int    executorRepairStuckWorkflowsMaxSeconds;

	public ConsistencyLevel getConsistencyLevel() {
		return consistencyLevel;
	}

	public ConsistencyLevel getDispatcherConsistencyLevel() {

		return dispatcherConsistencyLevel;
	}

	public String getDatabaseKeyspace() {

		return databaseKeyspace;
	}

	public int getDatabaseReplicas() {

		return databaseReplicas;
	}

	public String getDatabaseDcName() {

		return databaseDcName;
	}

	public int getDatabaseBucketSize() {

		return databaseBucketSize;
	}


	public String getExecutorGroup() {

		return executorGroup;
	}

	public int getDispatcherFetchSize() {

		return dispatcherFetchSize;
	}

	public int getExecutorThreadPoolSize() {

		return executorThreadPoolSize;
	}

	public int getExecutorMaxExecutionCount() {

		return executorMaxExecutionCount;
	}

	public int getExecutorRepairStuckWorkflowsMaxSeconds() {

		return executorRepairStuckWorkflowsMaxSeconds;
	}

	public String getDatabaseIp() {

		return databaseIp;
	}

	public int getDatabasePort() {

		return databasePort;
	}

	public boolean isExecutorEnabled() {

		return executorEnabled;
	}

	public String getAppVersion() {

		return appVersion;
	}
}
