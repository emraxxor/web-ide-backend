package hu.emraxxor.web.ide.components.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Base64Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

import hu.emraxxor.web.ide.config.ApplicationUser;
import hu.emraxxor.web.ide.data.type.UserFormElement;
import hu.emraxxor.web.ide.data.type.SimpleUserNameAndPassword;
import lombok.SneakyThrows;

/**
 * 
 * @author attila
 *
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter  {
	
		private AuthenticationManager authenticationManager;
	    
	    private static final String COOKIE_NAME = "token";

	    private static final int EXPIRATION =  30 * 60 * 1000;
	    
	    private String secret;

	    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, String secret) {
	        this.authenticationManager = authenticationManager;
	        this.secret = secret;
	        setFilterProcessesUrl("/authenticate"); 
	    }

	    @Override
	    @SneakyThrows
	    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
	       var creds = new ObjectMapper().readValue(req.getInputStream(), SimpleUserNameAndPassword.class);

	       return authenticationManager.authenticate(
	                    new UsernamePasswordAuthenticationToken(
	                            creds.getUsername(),
	                            creds.getPassword(),
	                            new ArrayList<>())
	            );
	    }

	    @Override
	    protected void successfulAuthentication(HttpServletRequest req,
	                                            HttpServletResponse res,
	                                            FilterChain chain,
	                                            Authentication auth) throws IOException {
	    	
	        var now = System.currentTimeMillis();
	        var mapper = new ModelMapper();
	        var user = (ApplicationUser)auth.getPrincipal();
	        var strategy = new ExclusionStrategy() {
				
				@Override
				public boolean shouldSkipField(FieldAttributes field) {
					 if (field.getDeclaringClass() == UserFormElement.class && field.getName().equals("userPassword")) 
		                    return true;
		             
					 return false;
				}
				
				@Override
				public boolean shouldSkipClass(Class<?> clazz) {
					return false;
				}
			};
			
			
			var gson = new GsonBuilder()
					  .addSerializationExclusionStrategy(strategy)
					  .create();

			
	        
	        String token = JWT.create()
	        		.withSubject(auth.getName())
	        		.withIssuedAt(new Date(now))
	        		.withExpiresAt(new Date(now + EXPIRATION))
	        		.withClaim("roles", ((ApplicationUser)auth.getPrincipal()).
	        				getGrantedAuthorities()
	        				.stream()
	        				.map(e -> e.getAuthority() ).collect(Collectors.toList()) 
	        		 )
	        		.withClaim("user", Base64Utils.encodeToString( 
	        								gson.toJson( mapper.map( user.getUser() , UserFormElement.class )).getBytes()  
	        						   ) 
	        		 )
	        		.withClaim("uid",  ((ApplicationUser) auth.getPrincipal()).getUser().getUserId() )
	        		.sign(Algorithm.HMAC512(secret.getBytes()));
	        

	        var cookie = new Cookie(COOKIE_NAME, token);
	        cookie.setMaxAge(EXPIRATION);
	        cookie.setPath("/");
	        cookie.setHttpOnly(true);
	        res.addCookie(cookie);

	        var resp = Maps.newHashMap();
	        resp.put("token", token);
	        resp.put("userId", user.getUserId());
	        resp.put("neptunId", user.getNeptunId());
	        res.getWriter().write(new ObjectMapper().writeValueAsString(resp));
	        res.getWriter().flush();
	    }
}
