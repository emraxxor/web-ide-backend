package hu.emraxxor.web.ide.data.type;

import javax.validation.constraints.NotNull;

import hu.emraxxor.web.ide.entities.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class ProjectFormElement extends FormElement<Project> {

	@NotNull
    private String name;

	private Long id;

    private String identifier;

    public ProjectFormElement(Project e) {
    	super(e);
    }
}
