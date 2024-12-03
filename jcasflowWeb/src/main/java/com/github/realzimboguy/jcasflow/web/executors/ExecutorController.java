package com.github.realzimboguy.jcasflow.web.executors;

import com.github.realzimboguy.jcasflow.engine.executor.WorkflowExecutor;
import com.github.realzimboguy.jcasflow.engine.repo.dao.ExecutorDao;
import com.github.realzimboguy.jcasflow.engine.repo.dao.WorkflowDefinitionDao;
import com.github.realzimboguy.jcasflow.engine.repo.entity.ExecutorEntity;
import com.github.realzimboguy.jcasflow.engine.repo.entity.WorkflowDefinitionEntity;
import com.github.realzimboguy.jcasflow.web.definition.DefinitionService;
import com.github.realzimboguy.jcasflow.web.definition.model.WorkflowDefinitionModel;
import com.github.realzimboguy.jcasflow.web.executors.model.ExecutorModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class ExecutorController {

	private static final String title = "Executors";

	private final DefinitionService definitionService;
	private final ExecutorDao       executorDao;

	public ExecutorController(DefinitionService definitionService, ExecutorDao executorDao) {

		this.definitionService = definitionService;
		this.executorDao = executorDao;
	}


	@GetMapping("executors")
	public String executors(Model model, HttpServletRequest request) {

		model.addAttribute("requestURI", request.getRequestURI());
		model.addAttribute("title", title);

		List<ExecutorEntity> executors = executorDao.getAll();
		// order by lastAlive desc
		executors.sort((o1, o2) -> o2.getLastAlive().compareTo(o1.getLastAlive()));

		List<ExecutorModel> executorModels = new ArrayList<>();

		for (ExecutorEntity executor : executors) {
			String cssClass = "";
			// if the last alive was within 5 min, set to green, if its within 10 min, set to yellow, otherwise grey
			//using tailwind css classes
			if (Duration.between(executor.getLastAlive(), Instant.now()).toMinutes() < 5) {
				cssClass = "bg-green-300";
			} else if (Duration.between(executor.getLastAlive(), Instant.now()).toMinutes() < 10) {
				cssClass = "bg-amber-200";
			} else {
				cssClass = "bg-gray-200";
			}

			executorModels.add(
					new ExecutorModel(
							executor.getId(),
							executor.getGroup(),
							executor.getHost(),
							DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.systemDefault()).format(executor.getStartedAt()),
							getFriendlyTimeAgo(executor.getLastAlive()),
							cssClass
					));
		}

		model.addAttribute("executors", executorModels);

		return "executors/executors.html";

	}

	private String getFriendlyTimeAgo(Instant lastAlive) {

		// Calculate the difference between current time and event time
		Duration duration = Duration.between(lastAlive, Instant.now());

		// Get the hours, minutes, and seconds
		long hours = duration.toHours();
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;

		// Create a user-friendly time string
		String timeAgo = "";
		if (hours > 0) {
			timeAgo += hours + "h ";
		}
		if (minutes > 0) {
			timeAgo += minutes + "m ";
		}
		if (seconds > 0) {
			timeAgo += seconds + "s ";
		}

		// Append 'ago'
		timeAgo += "ago";

		return timeAgo;

	}


}
