package hu.emraxxor.web.ide.config;

import static hu.emraxxor.web.ide.config.ApplicationPermission.*;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.common.collect.Sets;

/**
 * 
 * @author Attila Barna
 *
 */
public enum ApplicationUserRole {

	USER(Sets.newHashSet(USER_READ, USER_WRITE, USER_CREATE, USER_DELETE, DOCKER_CONTAINER_PERMISSION)),
	
	ADMIN(Sets.newHashSet(
				ADMIN_READ, ADMIN_WRITE, ADMIN_CREATE, ADMIN_DELETE, 
				USER_READ, USER_WRITE, USER_CREATE, USER_DELETE,
				DOCKER_EXEC_PERMISSION, DOCKER_CONTAINER_PERMISSION,
				DOCKER_ADMIN_PERMISSION
	)),

	DOCKER_MANAGER(Sets.newHashSet(DOCKER_EXEC_PERMISSION, DOCKER_CONTAINER_PERMISSION))
	
	;
	
	private final Set<ApplicationPermission> permissions;
	
	ApplicationUserRole(Set<ApplicationPermission> p) {
		this.permissions = p;
	}
	
	public List<SimpleGrantedAuthority> grantedAuthorities() {
		List<SimpleGrantedAuthority> ps = permissions.stream().map(p -> new SimpleGrantedAuthority(p.get())).collect(Collectors.toList());	
		ps.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
		return ps;
	}
}
