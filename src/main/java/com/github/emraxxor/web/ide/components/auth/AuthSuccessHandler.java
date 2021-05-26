package com.github.emraxxor.web.ide.components.auth;

import com.github.emraxxor.web.ide.config.ApplicationUser;
import com.github.emraxxor.web.ide.service.UserService;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

	private final UserService userService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final ApplicationUser au = (ApplicationUser)authentication.getPrincipal();
	}

}
