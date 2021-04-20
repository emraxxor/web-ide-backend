package hu.emraxxor.web.ide.data.type;

import hu.emraxxor.web.ide.entities.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserProfileFormElement extends FormElement<User>  {

    @NotNull @NotBlank
    private String firstName;

    @NotNull @NotBlank
    private String lastName;

    private String address;

    private String city;

    private String state;

    private Number zip;

}
