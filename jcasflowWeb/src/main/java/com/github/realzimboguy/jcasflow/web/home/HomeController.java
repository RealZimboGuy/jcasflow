package com.github.realzimboguy.jcasflow.web.home;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private static final String title = "Dashboard";

	private final HomeService homeService;

	public HomeController(HomeService homeService) {

		this.homeService = homeService;
	}

	@GetMapping("/")
	public String home(Model model, HttpServletRequest request) {

		model.addAttribute("requestURI", request.getRequestURI());
		model.addAttribute("title",title );

		return "home/home.html";

	}
	@GetMapping("/home/inprogresscount")
	public String inProgressCount(Model model, HttpServletRequest request) {

		model.addAttribute("workflowsInProgress", homeService.getWorkflowsInProgress());
		return "home/inProgressCount.html";

	}

	@GetMapping("/home/nextexecutioncount")
	public String nextExecutionCount(Model model, HttpServletRequest request) {

		model.addAttribute("nextExecution", homeService.getWorkflowsNextExecution());
		return "home/nextExecutionCount.html";

	}


}
