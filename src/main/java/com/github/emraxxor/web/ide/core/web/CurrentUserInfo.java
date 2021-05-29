package com.github.emraxxor.web.ide.core.web;

public interface CurrentUserInfo<T> {

	Boolean isAuthenticated();
	
	DefaultApplicationRole getRole();
	
	T getUser();
}
