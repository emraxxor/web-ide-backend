package com.github.emraxxor.web.ide.data.type;

import com.github.emraxxor.web.ide.entities.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserProfileFormElement extends FormElement<User>  {

    @NotNull @NotBlank
    @ApiModelProperty(value = "Firstname", required = true)
    private String firstName;

    @NotNull @NotBlank
    @ApiModelProperty(value = "Lastname", required = true)
    private String lastName;

    @ApiModelProperty(value = "Address")
    private String address;

    @ApiModelProperty(value = "City")
    private String city;

    @ApiModelProperty(value = "State")
    private String state;

    @ApiModelProperty(value = "Zip")
    private Integer zip;

}
