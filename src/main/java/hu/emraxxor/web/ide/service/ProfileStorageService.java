package hu.emraxxor.web.ide.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import hu.emraxxor.web.ide.config.ProfileProperties;
import hu.emraxxor.web.ide.data.type.DefaultFileInfo;
import hu.emraxxor.web.ide.data.type.FileInfo;
import hu.emraxxor.web.ide.data.type.ImageData;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author attila
 *
 */
@Service
@Log4j2
public class ProfileStorageService {

	
	@Autowired
	private ProfileProperties fp;
	
	@PostConstruct
	@SneakyThrows
	public void init() {
		var fold = new File( fp.getStorage() );
		if ( !fold.exists() )
			FileUtils.forceMkdir(fold);
	}
	
	private File storeFile(byte[] data) {
		File f = generateRandomFileName();
		FileOutputStream fos = null;
		try {
			f.createNewFile();
			fos = new FileOutputStream(f);
			fos.write(data);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if ( fos != null ) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
				}
			}
		}
		return f;
	}
	
	private File generateRandomFileName() {
		String path = fp.getStorage();
		String randomFileName = RandomStringUtils.random(50, true, true);
		File f;
		
		while( ((f=new File(path + "/" + randomFileName )).exists())  ) 
			randomFileName = RandomStringUtils.random(50, true, true);
		
		return f;
	}
	
	@Synchronized
	public ImageData file(String name) throws IOException {
		return new ImageData(Base64Utils.encodeToString( FileUtils.readFileToByteArray( new File(fp.getStorage() + "/" + name) ) ) );
	}
	
	@Synchronized
	@SneakyThrows
	public boolean remove(String name) {
		return new File(this.fp+"/"+name).delete();
	}
	
	@Synchronized
	public FileInfo storeFile(String base64data) {
		return new DefaultFileInfo( storeFile(Base64Utils.decodeFromString(base64data)) );
	}
}
