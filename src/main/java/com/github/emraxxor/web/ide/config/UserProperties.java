package com.github.emraxxor.web.ide.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;


/**
 * 
 * @author attila
 *
 */
@Configuration
@ConfigurationProperties(prefix = "user")
@Data
public class UserProperties {
	private String storage;
}
