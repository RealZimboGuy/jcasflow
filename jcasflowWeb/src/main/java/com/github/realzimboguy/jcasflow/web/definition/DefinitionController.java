package com.github.realzimboguy.jcasflow.web.definition;

import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDefinitionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowDefinitionEntity;
import com.github.realzimboguy.jcasflow.web.definition.model.WorkflowDefinitionModel;
import com.github.realzimboguy.jcasflow.web.settings.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DefinitionController {

	private static final String title = "Settings";

	private final DefinitionService definitionService;
	private final WorkflowDefinitionDao workflowDefinitionDao;

	public DefinitionController(DefinitionService definitionService, WorkflowDefinitionDao workflowDefinitionDao) {

		this.definitionService = definitionService;
		this.workflowDefinitionDao = workflowDefinitionDao;
	}


	@GetMapping("definitions")
	public String home(Model model, HttpServletRequest request) {

		model.addAttribute("requestURI", request.getRequestURI());
		model.addAttribute("title",title );

		List<WorkflowDefinitionEntity> workflowDefinitionEntities = workflowDefinitionDao.getAll();

		List<WorkflowDefinitionModel> workflowDefinitionModels = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (WorkflowDefinitionEntity workflowDefinitionEntity : workflowDefinitionEntities) {
			workflowDefinitionModels.add(new WorkflowDefinitionModel(
					workflowDefinitionEntity.getName(),
					workflowDefinitionEntity.getFlowChart(),
					sdf.format(workflowDefinitionEntity.getCreated()),
					sdf.format(workflowDefinitionEntity.getUpdated())
			));
		}

		model.addAttribute("definitions", workflowDefinitionModels);

		return "definition/definitions.html";

	}

}
