package hu.emraxxor.web.ide.data.type.docker;

import java.util.Arrays;
import java.util.Optional;

/**
 * 
 * @author Attila Barna
 *
 */
public enum DockerContainerImage {

	NPM("web-ide/npm"),
	NODE("web-ide/node", true, true),
	LARAVEL("web-ide/laravel"),
	PHP("web-ide/php", true, true)
	;
	
	private String image;
	
	private final Boolean tty;
	
	private final Boolean attachStdin;
	
	private final String workdir;
	
	DockerContainerImage(String image) {
		this(image,false,false);
	}
	
	DockerContainerImage(String image, Boolean tty,Boolean attachStdin) {
		this(image,tty,attachStdin,"/app");
	}

	DockerContainerImage(String image, Boolean tty,Boolean attachStdin, String workdir) {
		this.image = image;
		this.tty = tty;
		this.attachStdin = attachStdin;
		this.workdir = workdir;
	}
	
	
	public Boolean tty() {
		return this.tty;
	}
	
	
	public Boolean attachStdin() {
		return this.attachStdin;
	}
	
	
	public String image() {
		return this.image;
	}
	
	public String workdir() {
		return this.workdir;
	}
	
	public static Optional<DockerContainerImage> findByName(final String tname) {
		return Arrays.asList( DockerContainerImage.values() )
				.stream()
				.filter( e -> e.name().equalsIgnoreCase( tname ))
				.findAny();
				
	}
}
