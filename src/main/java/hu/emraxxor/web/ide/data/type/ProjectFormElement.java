package hu.emraxxor.web.ide.data.type;

import javax.validation.constraints.NotNull;
import com.github.dockerjava.api.model.Container;
import hu.emraxxor.web.ide.entities.Project;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Attila Barna
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProjectFormElement extends FormElement<Project> {

	@NotNull
	@ApiModelProperty(notes = "Name of the project", name = "name", example = "name_of_the_project" )
    private String name;

	@ApiModelProperty(notes = "Id of the project", required = true, example = "1")
	private Long id;

	@ApiModelProperty(notes = "Unique identifier of the project", required = true, example = "project_unique_id")
    private String identifier;
    
    @IgnoreField
    @ApiModelProperty(hidden = true)
    private Container container;

    @IgnoreField
    @ApiModelProperty(hidden = true)
    private ProjectFormContainerElement containerElement;

    public ProjectFormElement(Project e) {
    	super(e);

    	if ( e.getContainer() != null ) {
            this.containerElement = new ProjectFormContainerElement(e.getContainer());
        }
    }
}
