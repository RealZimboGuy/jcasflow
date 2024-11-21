package com.github.realzimboguy.casflow.executor;

import com.github.realzimboguy.casflow.config.JCasFlowConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ExecutorManager {

	private final JCasFlowConfig jCasFlowConfig;

	private ThreadPoolExecutor executor;

	public ExecutorManager(JCasFlowConfig jCasFlowConfig) {

		this.jCasFlowConfig = jCasFlowConfig;
	}

	@PostConstruct
	public void setup() {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(jCasFlowConfig.getExecutorThreadPoolSize());
	}



	public boolean hasFreeThreads() {
		if (executor.getActiveCount() < jCasFlowConfig.getExecutorThreadPoolSize()) {
			return true;
		}
		return false;
	}


	public void submit(WorkflowExecutor workflowExecutor) {
		executor.submit(workflowExecutor);
	}
}
