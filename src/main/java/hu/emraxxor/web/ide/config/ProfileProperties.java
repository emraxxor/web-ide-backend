package hu.emraxxor.web.ide.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;


/**
 * 
 * @author attila
 *
 */
@Configuration
@ConfigurationProperties(prefix = "profile")
@Data
public class ProfileProperties {
	private String storage;
}
