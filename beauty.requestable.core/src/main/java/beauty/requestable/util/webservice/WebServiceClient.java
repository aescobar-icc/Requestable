package beauty.requestable.util.webservice;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import beauty.requestable.util.stream.StreamUtil;

public class WebServiceClient {
	private URL url;
	private HttpURLConnection conn;
	protected String webServiceName = "WebServiceClient";
	
	public WebServiceClient(String url) throws MalformedURLException{
		this.url = new URL(url);
	}
	public void  createSecureContext(String pathCertificado,String password) throws FileNotFoundException{
			FileInputStream fis = new FileInputStream(pathCertificado);
			createSecureContext(fis, password);
	}
	/**
	 * 
	 * @param input
	 * @param password
	 * @throws RuntimeException si la creacion de la conexión falla por algún motivo
	 */
	public void  createSecureContext(InputStream input,String password){
		try{
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(input, password.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, password.toCharArray());
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			conn = (HttpURLConnection) url.openConnection();
			if(HttpsURLConnection.class.isAssignableFrom(conn.getClass()))
				((HttpsURLConnection)conn).setSSLSocketFactory(sc.getSocketFactory());
			
		}catch(Throwable e){
			throw new RuntimeException("[WebServiceClient.createSecureContext] ERROR: creando conexión segura",e);
		}
	}
	public String invokeOperation(String soapAction,String soapRequest){
		ByteArrayOutputStream rs;
		try{
			if(conn == null){
				conn = (HttpURLConnection) url.openConnection();
			}
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "text/xml"); 
			conn.setRequestProperty("charset", "UTF-8");
			conn.setRequestProperty("Content-Length", "" + soapRequest.getBytes().length);
			conn.setRequestProperty("SOAPAction", soapAction);
	
			conn.setDoOutput(true);
			conn.setDoInput(true);
				
			//System.out.println("[WebServiceClient] writing :"+soapRequest);		
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(soapRequest);
			out.flush();
			out.close();
	
			//System.out.println("[WebServiceClient] reading bytes...");	
			InputStream in = conn.getInputStream();
			rs = StreamUtil.readInputStream(in);
			in.close();
			conn = null;
			return new String(rs.toByteArray(),"UTF-8");
		}catch(Throwable e){
			throw new RuntimeException(String.format("[WebServiceClient.invokeOperation] ERROR: ejecutando operacion:%s de web service:%s",soapAction,webServiceName),e);
		}
	}
	
	public String getWebServiceName() {
		return webServiceName;
	}
	public void setWebServiceName(String webServiceName) {
		this.webServiceName = webServiceName;
	}
	
}
