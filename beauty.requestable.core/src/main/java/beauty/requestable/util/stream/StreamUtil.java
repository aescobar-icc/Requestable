package beauty.requestable.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import beauty.requestable.util.bytes.BytesUtil;

public class StreamUtil {

	public static ByteArrayOutputStream readInputStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    int length = 0;
	    while ((length = inputStream.read(buffer)) != -1) {
	        baos.write(buffer, 0, length);
	    }
	    
	    return baos;
	}
	public static ByteArrayOutputStream readInputStream(InputStream inputStream,int length) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buffer = new byte[512];
	    int read = 0;
	    int totalRead = 0;
	    while (totalRead < length && (read = inputStream.read(buffer)) != -1) {
	        baos.write(buffer, 0, read);
	        totalRead += read;
	    }
	    return baos;
	}
	public static ByteArrayOutputStream readInputStream(InputStream inputStream,byte[] delimiter,boolean inclusive) throws IOException {
	    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    int b = 0;
	    int i; 
	   // System.out.println("----------------------reading input----------------------");
	    while ((b = inputStream.read()) != -1) {
	    	tempStream.write(b);
	    	//System.out.print((char)b);
	    	if(BytesUtil.contains(tempStream.toByteArray(),delimiter)){	    		
	    		if(inclusive)
	    			i = BytesUtil.indexOf(tempStream.toByteArray(),delimiter)+delimiter.length;
	    		else
	    			i = BytesUtil.indexOf(tempStream.toByteArray(),delimiter);
	    		baos.write(tempStream.toByteArray(), 0, i);
	    		break;
	    	}
	    }
    	//System.out.print("--"+new String(tempStream.toByteArray())+"--");
	    
	    return baos;
	}
	public static void copyStream(InputStream inputStream,OutputStream outputStream) throws IOException {
	    byte[] buffer = new byte[1024];
	    int length = 0;
	    while ((length = inputStream.read(buffer)) != -1) {
	    	outputStream.write(buffer, 0, length);
	    }
	    
	}
}
