package com.github.realzimboguy.jcasflow.example;

import com.github.realzimboguy.jcasflow.engine.executor.WorkflowBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {
		"com.github.realzimboguy.jcasflow.example",
		"com.github.realzimboguy.jcasflow.engine"
})
public class JcasflowExampleApplication {


	public static void main(String[] args) {

		SpringApplication.run(JcasflowExampleApplication.class, args);
	}


}
