package hu.emraxxor.web.ide.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultModelMapper {

	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}

}
