package com.github.realzimboguy.jcasflow.web.settings;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class SettingsController {

	private static final String title = "Settings";

	private final SettingsService settingsService;

	public SettingsController(SettingsService settingsService) {

		this.settingsService = settingsService;
	}

	@GetMapping("settings")
	public String home(Model model, HttpServletRequest request) {

		model.addAttribute("requestURI", request.getRequestURI());
		model.addAttribute("title",title );

		model.addAttribute("settings", settingsService.getSettings());

		return "settings/settings.html";

	}

}
