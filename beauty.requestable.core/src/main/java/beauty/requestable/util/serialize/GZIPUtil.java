package beauty.requestable.util.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class GZIPUtil {
	
	public static ByteArrayOutputStream zipObject(Object obj) throws IOException{
	  ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
  	  GZIPOutputStream gz = new GZIPOutputStream(tempOut);
  	  ObjectOutputStream oos = new ObjectOutputStream(gz);
  	  oos.writeObject(obj);
  	  oos.flush();
  	  oos.close();
  	  return tempOut;
	}

	public static Object unzipObject(byte[] bytes) throws IOException, ClassNotFoundException{
		return unzipObject(new ByteArrayInputStream(bytes));
	}
	public static Object unzipObject(InputStream input) throws IOException, ClassNotFoundException{
		GZIPInputStream gis = new GZIPInputStream(input);
		ObjectInputStream ois = new ObjectInputStream(gis);
		return ois.readObject();
	}

}
