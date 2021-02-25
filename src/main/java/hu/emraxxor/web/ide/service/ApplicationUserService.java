package hu.emraxxor.web.ide.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import hu.emraxxor.web.ide.config.ApplicationUser;
import hu.emraxxor.web.ide.config.ApplicationUserRole;
import hu.emraxxor.web.ide.entities.User;
import hu.emraxxor.web.ide.repositories.UserRepository;

/**
 * 
 * @author Attila Barna
 *
 */
@Service
public class ApplicationUserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
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
