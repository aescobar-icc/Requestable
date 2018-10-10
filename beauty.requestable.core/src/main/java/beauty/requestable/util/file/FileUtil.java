package beauty.requestable.util.file;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FileUtil {
	
	/**
	 * find all files with certain extension
	 * */
	public static List<String> find(String[] paths,String extension){
		List<String> files = new ArrayList<String>();
		String name;
		for(String path:paths){
			File p = new File(path);
			if(p.exists()){
				name = p.getName().toLowerCase();
				if(p.isFile()){
					if(name.endsWith(extension)){
						//System.out.println(String.format("file found:%s",name));
						files.add(p.getAbsolutePath());
					}
					else if (name.endsWith(".jar")) {
						//System.out.println(name);
						findInJar(p,extension,files);
	                }
					else if (name.endsWith(".zip")) {
						//System.out.println(name);
	                }
				}else if(p.isDirectory()){
					String subs[] = p.list();
					//System.out.println("listing:"+p.getPath());
					for(int i=0;i<subs.length;i++){
						subs[i] = p.getPath() +File.separator+subs[i];
						//System.out.println("\t\t"+subs[i]);
					}
					files.addAll(find(subs, extension));
				}
			}
		}
		return files;
	}

	private static void findInJar(File f, String extension, List<String> files) {
        	JarInputStream jarFile = null;
	        try{
				jarFile = new JarInputStream(new FileInputStream(f));
	            JarEntry jarEntry;

	            while(true) {
	                jarEntry=jarFile.getNextJarEntry ();
	                if(jarEntry == null){
	                    break;
	                }
	                if((jarEntry.getName ().endsWith (extension)) ) {
						//System.out.println(String.format("file found in jar %s:%s",f.getName(),jarEntry.getName()));
	                	files.add(String.format("jar:%s",jarEntry.getName()));
	                }
	            }
	        }
	        catch( Exception e){
	            e.printStackTrace ();
	        }finally{
	        	if(jarFile != null)
					try {
						jarFile.close();
					} catch (IOException e) {}	        		
	        }
		
	}
	public static void writeFile(String filePath, byte[] fileBytes) throws IOException {
		if(!FileLockManager.isAvailable(filePath))
			throw new IOException("The file is locked");
		BufferedOutputStream bs = null;
	    FileOutputStream fs = new FileOutputStream(new File(filePath));
	    bs = new BufferedOutputStream(fs);
	    bs.write(fileBytes);
	    bs.flush();
	    if(!FileLockManager.isLock(filePath))
	    	fs.close();
	}
	public static byte[] readFile(String filePath) throws IOException {
		if(!FileLockManager.isAvailable(filePath))
			throw new IOException("The file is locked");
		byte[] buffer = new byte[1024];
		int length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    FileInputStream fs = new FileInputStream(new File(filePath));
	    while((length = fs.read(buffer)) != -1){
	    	out.write(buffer, 0, length);
	    }
	    if(!FileLockManager.isLock(filePath))
	    	fs.close();
		return out.toByteArray();
	}
	public static byte[] readFile(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    while((length = in.read(buffer)) != -1){
	    	out.write(buffer, 0, length);
	    }
		return out.toByteArray();
	}
	
	public static boolean deleteFile(String filePath) throws IOException{
		if(!FileLockManager.isAvailable(filePath))
			throw new IOException("The file is locked");
		if(FileLockManager.isLock(filePath))
			FileLockManager.unlockFile(filePath);
		return new File(filePath).delete();
	}
	public static boolean createDir(String filePath){
		File dir = new File(filePath);
		if(dir.exists())
			return true;
		return dir.mkdirs();
	}
	public static String removeExtension(String fileName){
		int i = fileName.lastIndexOf(".");
		if(i > 0){
			return fileName.substring(0, i);
		}
		return fileName;
	}
	public static String getLastModifiedTag(String filePath) {
			File file = new File(filePath);
			return String.format("%s-%s", file.length(),file.lastModified());
	}
	 /* for testing purposes only
	 public void run() {
			String filePath = "/Users/aescobar/Documents/scripts/producto_precio_espe.sql";
			try {
		        System.out.println("Hello from a thread!");
				System.out.println(new String(readFile(filePath),"utf-8"));
			} catch (IOException e) {
				System.out.println("run");
				e.printStackTrace();
			}
	    }

	  
		public static void main(String[] args) {
			System.out.println("Thread:"+Thread.currentThread().getId());
			String filePath = "/Users/aescobar/Documents/scripts/producto_precio_espe.sql";
			
			try {
				FileLockManager.lockFile(filePath);
				System.out.println(new String(readFile(filePath),"utf-8"));

		        (new Thread(new FileUtil())).start();
		        System.in.read();
		        FileLockManager.unlockFile(filePath);
		        (new Thread(new FileUtil())).start();
			} catch (Exception e) {
				Throwable t;
				do{
					t = e.getCause();
				}while(t!= null && t.getCause() != null);
				if(t !=null)
					System.out.println(t.getMessage());	
				e.printStackTrace();
			}
		}*/
}
