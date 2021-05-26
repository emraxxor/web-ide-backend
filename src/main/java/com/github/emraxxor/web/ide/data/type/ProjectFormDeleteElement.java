package com.github.emraxxor.web.ide.data.type;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFormDeleteElement {

    @NotNull
    @ApiModelProperty(notes = "Id of the project", required = true, example = "1")
    private Long id;
}
