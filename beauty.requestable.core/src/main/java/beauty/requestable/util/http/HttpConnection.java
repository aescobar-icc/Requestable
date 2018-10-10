package beauty.requestable.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class HttpConnection {
	private Socket 			client = null;
	private OutputStream	writer = null;
	private InputStream		reader  = null;
	private HttpProtocol protocol = new HttpProtocol();
	
	//private boolean isConnect = false;
	
	public HttpConnection(String url) throws UnknownHostException, IOException{
		protocol.setURL(url);
	}
	
	public ByteArrayOutputStream request(byte[] content,HTTPMethod method){
		try {
			client = new Socket(protocol.getHost(), protocol.getPort());
			writer  = client.getOutputStream();
			reader  = client.getInputStream();	
			
			byte[] requestBytes;
			protocol.setContent(content);
			protocol.setMethod(method);
			while(true){
				requestBytes = protocol.processConnection(reader);
				if(requestBytes == null)
					break;
				
				//System.out.println("WRITE:"+new String(responseBytes));
				if(requestBytes.length > 0){
					writer.write(requestBytes);
					writer.flush();
				}
				
			}
	
			writer.close();
			reader.close();
			client.close();

		}catch (Exception e) {
			e.printStackTrace();
			//throw new ServiceException("Error connecting with server",e);
		}
		return null;
	}
	

	public static void main(String[] args) {
		HttpConnection conn;
		try {
			conn = new HttpConnection("http://localhost:8080/Sphinx");
		    //conn.request(null,HTTPMethod.GET);
		    conn.request("base=PCF&login=aescobar&password=Frozen.1702".getBytes(),HTTPMethod.POST);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
