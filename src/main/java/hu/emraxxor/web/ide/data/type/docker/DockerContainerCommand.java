package hu.emraxxor.web.ide.data.type.docker;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
	private Long projectId;

	@NotEmpty
	private String image;

	@NotEmpty
	private String exposed;
	
}
