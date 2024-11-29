package com.github.realzimboguy.jcasflow.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.github.realzimboguy.jcasflow.web",
		"com.github.realzimboguy.jcasflow.engine"
})
public class JcasflowWebApplication {

	public static void main(String[] args) {

		SpringApplication.run(JcasflowWebApplication.class, args);
	}

}
