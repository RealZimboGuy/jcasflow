package com.github.realzimboguy.jcasflow.web.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home(Model model) {

		return "home/home.html";
	}


}
