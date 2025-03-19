// src/main/java/com/ricklee/community/Application.java
package com.ricklee.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  // BaseTimeEntity의 자동 시간 설정 활성화
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}