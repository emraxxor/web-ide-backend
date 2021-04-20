package hu.emraxxor.web.ide.data.type;

import hu.emraxxor.web.ide.entities.User;
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
    private String userMail;

    @NotNull @NotBlank
    @IgnoreField
    private String userPassword;

    @NotNull @NotBlank
    @IgnoreField
    private String confirmUserPassword;

    @IgnoreField
    private String oldUserPassword;

}
