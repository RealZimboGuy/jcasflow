package com.github.realzimboguy.jcasflow.web.settings;

import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.web.settings.model.SettingsModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {

	private final JCasFlowConfig jCasFlowConfig;

	public SettingsService(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	public List<SettingsModel> getSettings() {

		return List.of(
				new SettingsModel("jcasflow.engine.app.version", jCasFlowConfig.getAppVersion()),
				new SettingsModel("jcasflow.database.ip", jCasFlowConfig.getDatabaseIp()),
				new SettingsModel("jcasflow.database.port", String.valueOf(jCasFlowConfig.getDatabasePort())),
				new SettingsModel("jcasflow.database.replicas", String.valueOf(jCasFlowConfig.getDatabaseReplicas())),
				new SettingsModel("jcasflow.database.dc.name", jCasFlowConfig.getDatabaseDcName()),
				new SettingsModel("jcasflow.database.consistencyLevel", jCasFlowConfig.getConsistencyLevel().name()),
				new SettingsModel("jcasflow.database.dispatcher.consistencyLevel", jCasFlowConfig.getDispatcherConsistencyLevel().name()),
				new SettingsModel("jcasflow.database.bucket.size", String.valueOf(jCasFlowConfig.getDatabaseBucketSize())),
				new SettingsModel("jcasflow.dispatcher.fetch.size", String.valueOf(jCasFlowConfig.getDispatcherFetchSize())),
				new SettingsModel("jcasflow.executor.enabled", String.valueOf(jCasFlowConfig.isExecutorEnabled())),
				new SettingsModel("jcasflow.executor.group", jCasFlowConfig.getExecutorGroup()),
				new SettingsModel("jcasflow.executor.max.execution.count", String.valueOf(jCasFlowConfig.getExecutorMaxExecutionCount())),
				new SettingsModel("jcasflow.executor.thread.pool.size", String.valueOf(jCasFlowConfig.getExecutorThreadPoolSize())),
				new SettingsModel("jcasflow.executor.repair.stuck.workflows.max.seconds", String.valueOf(jCasFlowConfig.getExecutorRepairStuckWorkflowsMaxSeconds()))
		);


	}
}
