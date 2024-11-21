package com.github.realzimboguy.casflow;

import com.github.realzimboguy.casflow.workflow.DemoWorkflow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CasFlowApplication {

	public static void main(String[] args) {

		SpringApplication.run(CasFlowApplication.class, args);
	}


//	@Bean
//	public DemoWorkflow demoWorkflow() {
//
//		return new DemoWorkflow();
//	}

}
