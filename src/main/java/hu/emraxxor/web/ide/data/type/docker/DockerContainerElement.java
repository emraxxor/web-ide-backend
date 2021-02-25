package hu.emraxxor.web.ide.data.type.docker;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DockerContainerElement {

	@NotNull @NotEmpty
	private String id;
}
