package hu.emraxxor.web.ide.core.web;

public interface CurrentWebUserSession<T> {

	/**
	 * Information about the current user
	 * @return
	 */
	public CurrentUserInfo<T> current();
}
