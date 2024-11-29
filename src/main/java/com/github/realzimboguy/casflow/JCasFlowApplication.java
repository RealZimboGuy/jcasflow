package com.github.realzimboguy.casflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JCasFlowApplication {

	public static void main(String[] args) {

		SpringApplication.run(JCasFlowApplication.class, args);
	}


//	@Bean
//	public DemoWorkflow demoWorkflow() {
//
//		return new DemoWorkflow();
//	}

}
