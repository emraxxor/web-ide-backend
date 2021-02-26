package hu.emraxxor.web.ide.data.type.docker;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DockerContainerExecCommand {

	/**
	 * Project id
	 */
	@NotNull
	private Long id;
	
	@NotNull @NotEmpty
	private String command;
}
