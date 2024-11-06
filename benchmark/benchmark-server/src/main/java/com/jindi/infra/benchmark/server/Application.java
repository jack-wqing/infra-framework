package com.jindi.infra.benchmark.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication(scanBasePackages = {"com.jindi.infra.benchmark"})
@EnableDubbo(scanBasePackages = "com.jindi.infra.benchmark.server.rpc")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
