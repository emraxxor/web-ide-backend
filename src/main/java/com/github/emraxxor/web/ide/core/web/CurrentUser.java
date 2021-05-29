package com.github.emraxxor.web.ide.core.web;

import com.github.emraxxor.web.ide.data.type.UserFormElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class CurrentUser<T extends UserFormElement> implements CurrentUserInfo<T> {

	private final Boolean isAuthenticated;

	private final DefaultApplicationRole role;
	
	private final T user;
	
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
