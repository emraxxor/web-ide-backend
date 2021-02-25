package hu.emraxxor.web.ide.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import hu.emraxxor.web.ide.core.web.CurrentUserInfo;
import hu.emraxxor.web.ide.core.web.CurrentWebUserSession;
import hu.emraxxor.web.ide.data.type.UserFormElement;
import hu.emraxxor.web.ide.entities.User;
import lombok.Data;

/**
 * 
 * @author Attila Barna
 *
 */
@Data
public class ApplicationUser implements UserDetails, CurrentWebUserSession<UserFormElement> {

	private final List<? extends GrantedAuthority> grantedAuthorities;
	
	private final String neptunId;
	
	private final String email;
	
	private final Long userId;
	
	private final String password;
	
	private final Boolean isAccountNonExpired;
	
	private final Boolean isAaccountNonLocked;
	
	private final Boolean isCredentialsNonExpired;
	
	private final Boolean isEnabled;
	
	private final User user;
	
	private CurrentUserInfo<UserFormElement> userInfo;
 	
	public ApplicationUser(List<? extends GrantedAuthority> grantedAuthorities, User u) {
		super();
		this.grantedAuthorities = grantedAuthorities;
		this.userId = u.getUserId();
		this.neptunId = u.getNeptunId();
		this.password = u.getUserPassword();
		this.email = u.getUserMail();
		this.isAccountNonExpired = u.getIsActive();
		this.isAaccountNonLocked = u.getIsActive();
		this.isCredentialsNonExpired = u.getIsActive();
		this.isEnabled = u.getIsActive();
		this.user = u;
	}

	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return grantedAuthorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return neptunId;
	}

	@Override
	public boolean isAccountNonExpired() {
		return isAccountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return isAaccountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return isCredentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}


	@Override
	public CurrentUserInfo<UserFormElement> current() {
		return userInfo;
	}
	
}
