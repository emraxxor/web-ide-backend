package hu.emraxxor.web.ide.data.type.docker;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * Represents a command that starts a new container
 * 
 * @author Attila Barna
 *
 */
@Data
public class DockerContainerCommand {
	
	@NotNull @Digits(fraction = 0, integer = 10)
	@ApiModelProperty(required = true, example = "1")
	private Long projectId;

	@NotNull
	@ApiModelProperty(notes = "Image type", name = "image", required = true, allowEmptyValue = false)
	private DockerContainerImage image;

	@NotNull
	@ApiModelProperty(notes = "Exposed port", required = true, example = "3000")
	private Integer exposed;
	
}
