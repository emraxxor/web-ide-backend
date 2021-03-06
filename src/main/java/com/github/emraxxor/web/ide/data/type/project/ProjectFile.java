package com.github.emraxxor.web.ide.data.type.project;

import javax.validation.constraints.NotNull;

import com.github.emraxxor.web.ide.core.web.validator.FileNameConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFile {
	
	ProjectFileType type;	
	
	@NotNull
	@FileNameConstraint
	String name;
	
	@NotNull
	String data;

	String parent;
}
