package com.github.emraxxor.web.ide.data.type;


import java.time.LocalDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.github.emraxxor.web.ide.config.ApplicationUserRole;
import com.github.emraxxor.web.ide.entities.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserFormElement extends FormElement<User> {

	private Long userId;
	
	@NotNull @NotBlank
	@ApiModelProperty(notes = "Id of the user", required = true, value = "Neptun id of the user")
	private String neptunId;

	@NotNull @NotBlank
	@IgnoreField
	@ApiModelProperty(notes = "Password", required = true,  value = "Password")
	private String userPassword;

	@NotNull @NotBlank
	@Email
	@ApiModelProperty(value = "E-mail of the user", required = true)
	private String userMail;

	@NotNull @NotBlank
	@ApiModelProperty(value = "First name of the user", required = true)
	private String firstName;

	@NotNull @NotBlank
	@ApiModelProperty(value = "Last name of the user", required = true)
	private String lastName;

	@ApiModelProperty(value = "Address")
	private String address;

	@ApiModelProperty(value = "City")
	private String city;

	@ApiModelProperty(value = "State")
	private String state;

	@ApiModelProperty(value = "Zip")
	private Integer zip;

	@ApiModelProperty(value = "Role of the user")
	private ApplicationUserRole role;

	private LocalDateTime createdOn;

	@IgnoreField
	@ApiModelProperty(value = "Old password", required = true)
	private String oldUserPassword;

	public UserFormElement(User u) {
		super(u);
	}

}
