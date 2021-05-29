package com.github.emraxxor.web.ide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

@Configuration
public class DockerConfiguration {

	@Bean
	public DockerClient client() {
		return DockerClientBuilder.getInstance().build();
	}
}
