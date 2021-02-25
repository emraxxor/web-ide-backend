package hu.emraxxor.web.ide.data.type.docker;

import java.util.Arrays;
import java.util.Optional;

/**
 * 
 * @author Attila Barna
 *
 */
public enum DockerContainerImage {

	NODE("web-ide/node");

	private String image;
	
	DockerContainerImage(String image) {
		this.image = image;
	}
	
	public String image() {
		return this.image;
	}
	
	public static Optional<DockerContainerImage> findByName(final String tname) {
		return Arrays.asList( DockerContainerImage.values() )
				.stream()
				.filter( e -> e.name().equalsIgnoreCase( tname ))
				.findAny();
				
	}
}
