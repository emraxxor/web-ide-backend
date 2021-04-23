package hu.emraxxor.web.ide.components.auth;

import hu.emraxxor.web.ide.config.ApplicationUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Attila Barna
 *
 */
@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final ApplicationUser au = (ApplicationUser)authentication.getPrincipal();
	}

}
