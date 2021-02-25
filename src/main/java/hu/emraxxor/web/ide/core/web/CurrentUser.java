package hu.emraxxor.web.ide.core.web;

import hu.emraxxor.web.ide.data.type.UserFormElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class CurrentUser<T extends UserFormElement> implements CurrentUserInfo<T> {

	private Boolean isAuthenticated;

	private DefaultApplicationRole role;
	
	private T user;
	
	public Boolean isAuthenticated() {
		return isAuthenticated;
	}

	@Override
	public DefaultApplicationRole getRole() {
		return role;
	}

	@Override
	public T getUser() {
		return user;
	}

}
