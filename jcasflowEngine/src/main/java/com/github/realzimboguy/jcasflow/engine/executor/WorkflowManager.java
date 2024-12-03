package com.github.realzimboguy.jcasflow.engine.executor;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDefinitionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowDefinitionEntity;
import com.github.realzimboguy.jcasflow.engine.workflow.JCasWorkFlow;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowManager {

	Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

	private final ApplicationContext    context;
    private final WorkflowDefinitionDao workflowDefinitionDao;

	@Value("${jcasflow.display.graph.errorClass:fill:#fc0,stroke:#f66,stroke-width:2px,color:#fff,stroke-dasharray: 5 5;}")
	private String graphErrorClass;
	@Value("${jcasflow.display.graph.doneClass:fill:#23d902,stroke:#007500,stroke-width:2px,color:#fff,stroke-dasharray: 5 5;}")
	private String graphDoneClass;
	@Value("${jcasflow.display.graph.startClass:fill:#022ff5,stroke-width:2px,color:#fff,stroke-dasharray: 5 5;}")
	private String graphStartClass;
	@Value("${jcasflow.display.graph.manualClass:fill:#33b4ff,stroke:#f66,stroke-width:2px,color:#fff,stroke-dasharray: 5 5;}")
	private String graphManualClass;
	@Value("${jcasflow.display.graph.normalClass:fill:#c2e0f2,color:#000;}")
	private String graphNormalClass;

	public WorkflowManager(ApplicationContext context, WorkflowDefinitionDao workflowDefinitionDao) {

		this.context = context;
		this.workflowDefinitionDao = workflowDefinitionDao;
	}

	@PostConstruct
	public void startup() {
		//on startup we need to look for all workflows, and store the name and flow diagram.

		// Get all beans that are subclasses of JCasWorkFlow
		Map<String, JCasWorkFlow> workflowBeans = context.getBeansOfType(JCasWorkFlow.class);

		// Iterate over the beans
		for (String s : workflowBeans.keySet()) {
			logger.info("Found workflow: {} {}",s, workflowBeans.get(s).getClass().getName());

			JCasWorkFlow jCasWorkFlow = workflowBeans.get(s);

			List<WorkflowAllowedTransition> permittedTransitions = jCasWorkFlow.permittedTransitions();
			StringBuilder sb = new StringBuilder();
			List<String> errorClasses = new ArrayList<>();
			List<String> doneClasses = new ArrayList<>();
			List<String> startClasses = new ArrayList<>();
			List<String> manualClasses = new ArrayList<>();
			List<String> normalClasses = new ArrayList<>();

			List<String> statesToError = new ArrayList<>();

			errorClasses.add(jCasWorkFlow.errorState().method());
			startClasses.add(jCasWorkFlow.startingState().method());

			sb.append("flowchart TD\n");
			for (WorkflowAllowedTransition transition : permittedTransitions) {
				WorkflowState workflowState = transition.getFrom();
				WorkflowState to = transition.getTo();

				if (!statesToError.contains(workflowState.name()) && workflowState.stateType() != WorkflowState.WorkflowStateType.END) {
					statesToError.add(workflowState.name());
				}
				if (!statesToError.contains(to.name()) && to.stateType() != WorkflowState.WorkflowStateType.END) {
					statesToError.add(to.name());
				}

				Gson gson = new Gson();

				if (workflowState.stateType() == WorkflowState.WorkflowStateType.END) {
					doneClasses.add(workflowState.name());
				}
				if (  to.stateType() == WorkflowState.WorkflowStateType.END) {
					doneClasses.add(to.name());
				}

				if (workflowState.stateType() == WorkflowState.WorkflowStateType.MANUAL) {
					manualClasses.add(workflowState.name());
				}
				if (workflowState.stateType() == WorkflowState.WorkflowStateType.NORMAL) {
					normalClasses.add(workflowState.name());
				}

				sb.append("    ").append(workflowState.name()).append(" ==>").append(" ").append(to.name()).append("(").append(to.method()).append(")\n");
				//show error state
			}

			for (String string : statesToError) {
				sb.append("    ").append(string).append(" --> ").append(jCasWorkFlow.errorState().name()).append("\n");
			}

			sb.append("    classDef errorClass ").append(graphErrorClass).append("\n");
			sb.append("    classDef doneClass ").append(graphDoneClass).append("\n");
			sb.append("    classDef startClass ").append(graphStartClass).append("\n");
			sb.append("    classDef manualClass ").append(graphManualClass).append("\n");
			sb.append("    classDef normalClass ").append(graphNormalClass).append("\n");
			for (String errorClass : errorClasses) {
				sb.append("    class ").append(errorClass).append(" errorClass;\n");
			}
			for (String doneClass : doneClasses) {
				sb.append("    class ").append(doneClass).append(" doneClass;\n");
			}
			for (String startClass : startClasses) {
				sb.append("    class ").append(startClass).append(" startClass;\n");
			}
			for (String manualClass : manualClasses) {
				sb.append("    class ").append(manualClass).append(" manualClass;\n");
			}
			for (String normalClass : normalClasses) {
				sb.append("    class ").append(normalClass).append(" normalClass;\n");
			}


			logger.info("Workflow: {} \n{}",jCasWorkFlow.getName(),sb.toString());

			WorkflowDefinitionEntity workflowDefinitionEntity = workflowDefinitionDao.get(jCasWorkFlow.getName());

			if (workflowDefinitionEntity == null) {
				logger.info("Saving workflow definition: {}",jCasWorkFlow.getName());
				workflowDefinitionEntity = new WorkflowDefinitionEntity();
				workflowDefinitionEntity.setName(jCasWorkFlow.getName());
				workflowDefinitionEntity.setDescription(jCasWorkFlow.getDescription());
				workflowDefinitionEntity.setFlowChart(sb.toString());
				workflowDefinitionEntity.setUpdated(java.time.Instant.now());
				workflowDefinitionEntity.setCreated(java.time.Instant.now());
				workflowDefinitionDao.save(workflowDefinitionEntity);

			} else {
				logger.info("Updating workflow definition: {}",jCasWorkFlow.getName());
				workflowDefinitionEntity.setFlowChart(sb.toString());
				workflowDefinitionEntity.setDescription(jCasWorkFlow.getDescription());
				workflowDefinitionEntity.setUpdated(java.time.Instant.now());
				workflowDefinitionDao.save(workflowDefinitionEntity);
			}



			//

		}



	}

}
