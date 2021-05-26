package com.github.emraxxor.web.ide.data.type.docker;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class DockerContainerExecCommand {

	/**
	 * Project id
	 */
	@NotNull
	@ApiModelProperty(notes = "Id of the project", required = true, example = "1")
	private Long id;
	
	@NotNull @NotEmpty
	@ApiModelProperty(notes = "Command that we want to run", required = true, example = "ls -la")
	private String command;
}
