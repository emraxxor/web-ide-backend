package com.github.emraxxor.web.ide.components.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Base64Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.github.emraxxor.web.ide.data.type.UserFormElement;
import lombok.SneakyThrows;
import lombok.Synchronized;

/**
 * 
 * @author attila
 *
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String COOKIE_NAME = "token";
    
    private final String secret;
    
    private Cookie cookie;
    
    public JWTAuthorizationFilter(AuthenticationManager authManager, String secret) {
        super(authManager);
        this.secret = secret;
    }

    @Override
    @Synchronized
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
    	
        var storedToken = getTokenFromCookie(req);
        if (storedToken.isEmpty()) {
            storedToken = getTokenFromHeader(req);
            if (storedToken.isEmpty()) {
                chain.doFilter(req, res);
                return;
            }
        }
        
        var token = storedToken.get();
        UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    @SneakyThrows
    @Synchronized
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
    	try {
			DecodedJWT jwt = JWT
								 .require(Algorithm.HMAC512(secret.getBytes()))
								 .build()
								 .verify(token);
					
			var user = jwt.getSubject();

			if (user != null) {
				var roles = jwt.getClaim("roles").asList(String.class);
				var principal =  new Gson().fromJson( 
									new String( Base64Utils.decodeFromString( jwt.getClaim("user").asString() ) ) ,
									new TypeToken<UserFormElement>(){}.getType()
								);
				return new UsernamePasswordAuthenticationToken(principal, null, roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
			}
    	} catch(Exception e) {
    		cookie.setMaxAge(-1);
    	}
        
        return null;
    }
    
    
    @Synchronized
    private Optional<String> getTokenFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) 
            return Optional.empty();
        
        var cookie = Arrays
        				.stream(req.getCookies())
        				.filter(c -> c.getName().equals(COOKIE_NAME))
        				.findAny();
        
        if (cookie.isEmpty())
            return Optional.empty();
        
        this.cookie = cookie.get();
        
        return cookie.map(Cookie::getValue);

    }
   
    @Synchronized
    private Optional<String> getTokenFromHeader(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        
        if (header == null || !header.startsWith("Bearer ")) 
            return Optional.empty();
        
        return Optional.of(header.replace("Bearer ", ""));
    }
}
