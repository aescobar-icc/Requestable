package beauty.requestable.util.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.HashMap;

public class FileLockManager {

	private static final Object lock = new Object();
	private static final HashMap<String, Long> owner = new HashMap<String, Long>();
	private static final HashMap<String, FileLockManager> fileInfo = new HashMap<String, FileLockManager>();
	
	private String filePath;
	private File file = null;
	private RandomAccessFile raf = null;
	private FileLock fileLock=null;


	private FileLockManager(String filePath) throws IOException {
        this.filePath	= filePath;
		this.file		= new File(filePath);
		file.setReadable(true,true);
		file.setWritable(true,true);
		this.raf		= new RandomAccessFile(file, "rw");
		this.fileLock	= raf.getChannel().tryLock();

		if(this.fileLock == null)
			throw new IOException(String.format("File:%s  is already locked for other java process",filePath));
		
		owner.put(filePath, Thread.currentThread().getId());
	}

	private static boolean isLockedForMe(String filePath){
    	synchronized(lock){
    		return owner.get(filePath) != null && owner.get(filePath) == Thread.currentThread().getId();
    	}
    }
    public static boolean isAvailable(String filePath){
    	synchronized(lock){
    		return owner.get(filePath) == null || owner.get(filePath) == Thread.currentThread().getId();
    	}
    }
    public static boolean isLock(String filePath){
    	synchronized(lock){
    		return owner.get(filePath) != null;
    	}
    }
	public  static void lockFile(String filePath) throws IOException {
		synchronized(lock){
			if(isLock(filePath))
					throw new IOException(String.format("File:%s is already locked",filePath));
			if(isLockedForMe(filePath))
				throw new IOException(String.format("File:%s  is already locked for me",filePath));
			
			FileLockManager mng = new FileLockManager(filePath);
			
			fileInfo.put(filePath, mng);
			
			System.out.println(String.format("File:%s  is locked successful",filePath));
		}
	}
	public  static void unlockFile(String filePath) throws IOException {
		synchronized(lock){
			if(!isLock(filePath))
					throw new IOException(String.format("File:%s  is not locked",filePath));
			if(!isLockedForMe(filePath))
				throw new IOException(String.format("File:%s  is locked for other thread",filePath));
			
			FileLockManager mng = fileInfo.get(filePath);
			mng.realese();
			System.out.println(String.format("File:%s  was realesed!",filePath));
		}
	}
	public void realese() throws IOException{
		raf.close();
		fileInfo.remove(filePath);
		owner.remove(filePath);
	}

}
