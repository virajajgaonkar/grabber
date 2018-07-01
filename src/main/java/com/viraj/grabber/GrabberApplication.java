package com.viraj.grabber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan
public class GrabberApplication {
	public static void main(String[] args) {
		SpringApplication.run(GrabberApplication.class, args);
	}
}