package com.github.emraxxor.web.ide.data.type.docker;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Attila Barna
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DockerContainerElement {

	@NotNull @NotEmpty
	private String id;
}
