package hu.emraxxor.web.ide.data.type;

import java.io.File;

/**
 * 
 * @author attila
 *
 */
public class DefaultFileInfo implements FileInfo {

	private String name;
	
	private Long lastModified;
	
	public DefaultFileInfo(File f) {
		name = f.getName();
		lastModified = f.lastModified();
	}
	
	@Override
	public Long lastModified() {
		return lastModified;
	}
	
	@Override
	public String name() {
		return name;
	}
}
