package hu.emraxxor.web.ide.config;

/**
 * 
 * @author Attila Barna
 *
 */
public enum ApplicationPermission {

	USER_CREATE("user:create"),
	USER_READ("user:read"),
	USER_WRITE("user:write"),
	USER_DELETE("user:delete"),
	
	ADMIN_CREATE("admin:create"),
	ADMIN_READ("admin:read"),
	ADMIN_WRITE("admin:write"),
	ADMIN_DELETE("admin:delete"),

	DOCKER_CONTAINER_PERMISSION("docker:container"),
	DOCKER_ADMIN_PERMISSION("docker:admin"),
	DOCKER_EXEC_PERMISSION("docker:exec"),
	
	ROLE_ADMIN("ADMIN"),
	ROLE_USER("USER"),
	ROLE_DOCKER_MANAGER("DOCKER_MANAGER")
	; 
	
	private final String u;
	
	ApplicationPermission(String n) {
		this.u = n;
	}
	
	public String get() {
		return this.u;
		
	}

}
