package hu.emraxxor.web.ide.data.type;

import java.time.LocalDateTime;

import hu.emraxxor.web.ide.data.type.docker.ContainerStatus;
import hu.emraxxor.web.ide.data.type.docker.DockerContainerImage;
import hu.emraxxor.web.ide.entities.Container;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContainerFormElement extends FormElement<Container> {

	private Long id;

	private DockerContainerImage image;

	private String name;

	private Integer bind;

	private Integer exposed;

	private String userdir;

	private String appdir;

	private String containerId;

	private ContainerStatus status;

	private String ip;

	private LocalDateTime updatedOn;

	private LocalDateTime createdOn;

	public ContainerFormElement(Container e) {
		super(e);
	}

}
