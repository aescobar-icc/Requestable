package beauty.requestable.util.properties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import beauty.requestable.util.crypto.UtilCrypto;
import beauty.requestable.util.file.FileUtil;

public class PropertiesLoader{
    private static final Object	sync  = new Object();
	private static final Timer	timer = new Timer();
	private static final HashMap<String,PropertiesLoader> files = new HashMap<String,PropertiesLoader>();
	
	/*
	 * schedule that reload file if changed
	 */
	static{
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (sync) {
					Set<String> filePaths = files.keySet();
					String currentETag;
					PropertiesLoader instance;
					for(String path:filePaths){
						currentETag = FileUtil.getLastModifiedTag(path);
						instance	= files.get(path);
						if(!currentETag.equals(instance.etag)){
							try {
								loadPropertyFile(instance);
							} catch (PropertiesException e) {
								System.err.println(e.toString());
							}
						}
					}
				}
			}
		},0, 1000);
	}
	
	protected Properties properties;
	private String fileName;
	private String etag;
	private boolean encrypted;
	
	public PropertiesLoader(String fileName) throws PropertiesException{	
		this(fileName,false,false);
	}
	public PropertiesLoader(String fileName,boolean useCatalinaBase,boolean encrypted) throws PropertiesException{		
		synchronized (sync) {
			if(useCatalinaBase)
					fileName = (new StringBuilder()).
						append(System.getProperty("catalina.base")).append(File.separator).append("conf").append(File.separator).append(fileName).toString();
			this.fileName	= fileName;
			this.encrypted	= encrypted;
			files.put(fileName, this);
			loadPropertyFile(this);
		}
	}
	private static void loadPropertyFile(PropertiesLoader instance) throws PropertiesException{
		synchronized (sync) {
			try {
				System.out.println(String.format("Loading properties: %s",instance.fileName));
				instance.etag = FileUtil.getLastModifiedTag(instance.fileName);
				InputStream input = new FileInputStream(instance.fileName);
				if(instance.encrypted){
					InputStream dinput = instance.decript(input);
					input.close();
					input = dinput;
				}
				instance.properties = new Properties();
				instance.properties.load(input);
				input.close();
			}catch (Throwable e) {
				throw new PropertiesException(String.format("Imposible cargar el archivo de propiedades %s.",instance.fileName),e);
			}
		}
	}
	private InputStream decript(InputStream input) throws IOException {
		String encryptedFile;
		try {
			encryptedFile = new String(FileUtil.readFile(input));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(String.format("Error leyendo archivo encripted %s",this.fileName),e);
		}	 
		String decryptedFile = UtilCrypto.decrypt(encryptedFile);
		return new ByteArrayInputStream(decryptedFile.getBytes());
	}
	public PropertiesLoader(InputStream input) throws PropertiesException{		
		
		try {
			properties = new Properties();
			properties.load(input);
			input.close();
		}catch (Exception e) {
			throw new PropertiesException("Imposible cargar el archivo de propiedades de inputstream.",e);
		}
		
	}
}