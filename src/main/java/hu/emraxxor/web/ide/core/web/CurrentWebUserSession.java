package hu.emraxxor.web.ide.core.web;

public interface CurrentWebUserSession<T> {

	/**
	 * Information about the current user
	 */
	CurrentUserInfo<T> current();
}
