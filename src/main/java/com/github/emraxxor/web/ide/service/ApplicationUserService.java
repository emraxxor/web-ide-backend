package com.github.emraxxor.web.ide.service;

import com.github.emraxxor.web.ide.config.ApplicationUserRole;
import com.github.emraxxor.web.ide.entities.User;
import com.github.emraxxor.web.ide.repositories.UserRepository;
import com.github.emraxxor.web.ide.config.ApplicationUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
@AllArgsConstructor
public class ApplicationUserService implements UserDetailsService {

	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
		Optional<User> user = userRepository.findByUserMail(mail);
		
		if ( user.isPresent() ) {
			var u = user.get();
			List<? extends GrantedAuthority> role = null;
			if ( u.getRole().equals( ApplicationUserRole.USER ) ) {
				role = ApplicationUserRole.USER.grantedAuthorities();
			} else if ( u.getRole().equals( ApplicationUserRole.ADMIN )  )  {
				role = ApplicationUserRole.ADMIN.grantedAuthorities();
			}
			
			return new ApplicationUser(role,u);
		} 
		return null;
	}
	

}
