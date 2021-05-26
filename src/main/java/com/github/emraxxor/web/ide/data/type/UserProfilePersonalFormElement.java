package com.github.emraxxor.web.ide.data.type;

import com.github.emraxxor.web.ide.entities.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserProfilePersonalFormElement extends FormElement<User>  {

    @NotNull
    @NotBlank
    @Email
    @ApiModelProperty(value = "E-mail", required = true)
    private String userMail;

    @NotNull @NotBlank
    @IgnoreField
    @ApiModelProperty(value = "Password", required = true)
    private String userPassword;

    @NotNull @NotBlank
    @IgnoreField
    @ApiModelProperty(value = "Password confirmation", required = true)
    private String confirmUserPassword;

    @IgnoreField
    @ApiModelProperty(value = "Old password")
    private String oldUserPassword;

}
