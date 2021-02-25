package hu.emraxxor.web.ide.core.web;

public interface CurrentUserInfo<T> {

	public Boolean isAuthenticated();
	
	public DefaultApplicationRole getRole();
	
	public T getUser();
}
