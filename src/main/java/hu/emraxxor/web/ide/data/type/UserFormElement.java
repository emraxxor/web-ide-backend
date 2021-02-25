package hu.emraxxor.web.ide.data.type;


import java.time.LocalDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import hu.emraxxor.web.ide.entities.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserFormElement extends FormElement<User> {

	private Long userId;
	
	@NotNull @NotBlank
    private String neptunId;

	@NotNull @NotBlank
	@IgnoreField
    private String userPassword;

	@NotNull @NotBlank
	@Email
    private String userMail;

	@NotNull @NotBlank
	private String firstName;

	@NotNull @NotBlank
    private String lastName;

	private String address;

	private String city;

	private String state;

	private Number zip;
	
	@IgnoreField
	private String role;
	
	private LocalDateTime createdOn;

}
