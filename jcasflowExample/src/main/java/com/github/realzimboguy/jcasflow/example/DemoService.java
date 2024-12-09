package com.github.realzimboguy.jcasflow.example;

import com.github.realzimboguy.jcasflow.engine.config.JCasFlowConfig;
import com.github.realzimboguy.jcasflow.engine.executor.WorkflowBuilder;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowNextExecutionDao;
import com.github.realzimboguy.jcasflow.engine.service.WorkflowBuilderService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

	private final WorkflowBuilderService workflowBuilderService;
	private final JCasFlowConfig           jCasFlowConfig;

	public DemoService(WorkflowBuilderService workflowBuilderService, JCasFlowConfig jCasFlowConfig) {

		this.workflowBuilderService = workflowBuilderService;
		this.jCasFlowConfig = jCasFlowConfig;
	}


	@PostConstruct
	public void init() {

		System.out.println("JcasflowExampleApplication starting and creating a workflow");

		WorkflowBuilder.builder()
				.withId(java.util.UUID.randomUUID())
				.withExecutorGroup(jCasFlowConfig.getExecutorGroup())
				.withWorkflowType("demoWorkflow")
				.withExternalId("external_id")
				.withBusinessKey("business")
				.withStateVar("var_name","value")
				.withNextExecution(java.time.ZonedDateTime.now().plusSeconds(5).toInstant())
				.build()
				.save(workflowBuilderService);

	}

}
