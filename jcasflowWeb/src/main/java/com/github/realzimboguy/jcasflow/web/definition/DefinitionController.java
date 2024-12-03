package com.github.realzimboguy.jcasflow.web.definition;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDefinitionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowDefinitionEntity;
import com.github.realzimboguy.jcasflow.web.definition.model.WorkflowDefinitionModel;
import com.github.realzimboguy.jcasflow.web.settings.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DefinitionController {

	private static final String title = "Workflow Definitions";

	private final DefinitionService definitionService;
	private final WorkflowDefinitionDao workflowDefinitionDao;

	public DefinitionController(DefinitionService definitionService, WorkflowDefinitionDao workflowDefinitionDao) {

		this.definitionService = definitionService;
		this.workflowDefinitionDao = workflowDefinitionDao;
	}


	@GetMapping("definitions")
	public String definitions(Model model, HttpServletRequest request) {

		model.addAttribute("requestURI", request.getRequestURI());
		model.addAttribute("title",title );

		List<WorkflowDefinitionEntity> workflowDefinitionEntities = workflowDefinitionDao.getAll();

		List<WorkflowDefinitionModel> workflowDefinitionModels = new ArrayList<>();

		for (WorkflowDefinitionEntity workflowDefinitionEntity : workflowDefinitionEntities) {
			workflowDefinitionModels.add(new WorkflowDefinitionModel(
					workflowDefinitionEntity.getName(),
					workflowDefinitionEntity.getFlowChart(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone( ZoneId.systemDefault() ).format(workflowDefinitionEntity.getCreated()),
					DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone( ZoneId.systemDefault() ).format(workflowDefinitionEntity.getUpdated())
			));
		}

		model.addAttribute("definitions", workflowDefinitionModels);

		return "definition/definitions.html";

	}

	@GetMapping("definitions/{name}")
	public String definitionById(Model model, HttpServletRequest request, @PathVariable String name) {

		WorkflowDefinitionEntity workflowDefinitionEntity = workflowDefinitionDao.get(name);

		model.addAttribute("requestURI", request.getRequestURI());
		model.addAttribute("title", "Workflow Definition - " + workflowDefinitionEntity.getName()  );

		model.addAttribute("workflowDefinition", workflowDefinitionEntity);

		return "definition/definitionByName.html";

	}

}
