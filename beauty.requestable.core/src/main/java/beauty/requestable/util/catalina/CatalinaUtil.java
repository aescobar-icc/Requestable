package beauty.requestable.util.catalina;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import beauty.requestable.util.file.FileLockManager;
import beauty.requestable.util.file.FileUtil;

public class CatalinaUtil {
	public static final String CATALINA_BASE = (new StringBuilder()).append(System.getProperty("catalina.base")).append(File.separator).toString();
	public static final String CATALINA_TEMP = (new StringBuilder(CATALINA_BASE)).append("temp").append(File.separator).toString();
	public static final String CATALINA_CONF = (new StringBuilder(CATALINA_BASE)).append("conf").append(File.separator).toString();
	private static File temp;
	static{
		temp = new File(CATALINA_TEMP);
		if(!temp.exists()){
			try{
				temp.mkdirs();
			}catch(Throwable e){
				System.err.println(String.format("[CatalinaUtil] Imposible crear %s", CATALINA_TEMP));
			}
		}
	}
	private static void checkTempExists() throws FileNotFoundException{
		if(!temp.exists())
			throw new FileNotFoundException("$CATALINA_BASE/temp does not exists");
	}
	public static void createTempFile(String fileName,byte[] file) throws IOException{
		checkTempExists();
		String path = getPath(fileName);
		//System.out.println(String.format("[CatalinaUtil] writing temp file:%s",path));
		FileUtil.writeFile(path, file);
	}
	public static byte[] readTempFile(String fileName) throws IOException{
		checkTempExists();
		String path = getPath(fileName);
		File p = new File(path);
		if(p.exists()){
			return FileUtil.readFile(path);
		}else
			throw new FileNotFoundException(String.format("The temp file:%s does not exists",path));
	}
	public static boolean existsTempFile(String fileName){
		String path = getPath(fileName);
		return new File(path).exists();
	}
	public static boolean deleteTempFile(String fileName) throws Exception{
		checkTempExists();
		String path = getPath(fileName);
		File temp = new File(path);
		if(temp.exists()){
			if(!FileUtil.deleteFile(path))
				throw new Exception(String.format("The temp file:%s could not be deleted",path));

			//System.out.println(String.format("[CatalinaUtil] deleting temp file:%s ",path));
			return true;
		}
		return false;
	}
	public static List<String> listTempFiles(String extension) throws FileNotFoundException{
		checkTempExists();
		return FileUtil.find(new String[]{CATALINA_TEMP}, extension);
	}
	private static String getPath(String fileName){
		String path = fileName;
		if(!fileName.startsWith(CATALINA_TEMP))
			path = (new StringBuilder(CATALINA_TEMP)).append(fileName).toString();
		return path;
	}
	public static void lockTempFile(String tempfileName) throws IOException {
		String path = getPath(tempfileName);
		FileLockManager.lockFile(path);
		
	}
	public static void unlockTempFile(String tempfileName) throws IOException {
		String path = getPath(tempfileName);
		FileLockManager.unlockFile(path);
	}
	public static String getServerName(){
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			String hostName =  localhost.getHostName();
			return hostName.replace("\\.intranet", "").replace(".local", "").toUpperCase();
		} catch (UnknownHostException e) {
		}
		return "";
	}
}
