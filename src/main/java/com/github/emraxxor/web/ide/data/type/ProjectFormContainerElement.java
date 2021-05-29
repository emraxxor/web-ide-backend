package com.github.emraxxor.web.ide.data.type;

import com.github.emraxxor.web.ide.data.type.docker.ContainerStatus;
import com.github.emraxxor.web.ide.entities.Container;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFormContainerElement extends FormElement<Container> {

    private String name;

    private Integer bind;

    private Integer exposed;

    private String userdir;

    private String appdir;

    private String containerId;

    private ContainerStatus status;

    private String ip;

    public ProjectFormContainerElement(Container data) {
        super(data);
    }

}
