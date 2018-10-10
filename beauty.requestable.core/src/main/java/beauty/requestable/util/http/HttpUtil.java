package beauty.requestable.util.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import beauty.requestable.util.stream.StreamUtil;

public class HttpUtil {
	private static SSLSocketFactory allTrustSocketFactory = null;
	private static HostnameVerifier allTrustHostnameVerifier = null;
	public enum RequestMethod{POST,GET};
	
	/**
	 * Caution: Many web sites describe a poor alternative solution which is to install a TrustManager that does nothing. 
	 * If you do this you might as well not be encrypting your communication, because anyone can attack your users at a public 
	 * Wi-Fi hotspot by using DNS tricks to send your users' traffic through a proxy of their own that pretends to be your server. 
	 * The attacker can then record passwords and other personal data. This works because the attacker can generate a certificate 
	 * and—without a TrustManager that actually validates that the certificate comes from a trusted source—your app could be talking to anyone. 
	 * So don't do this, not even temporarily. You can always make your app trust the issuer of the server's certificate, so just do it.
	 * REFERENCE:https://developer.android.com/training/articles/security-ssl.html
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void turnOffCertificateValidation() throws NoSuchAlgorithmException, KeyManagementException{

		// Install the all-trusting trust manager
		HttpsURLConnection.setDefaultSSLSocketFactory(getAllTrustSocketFactory());
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(getAllTrustHostnameVerifier());
	}
	public static HostnameVerifier getAllTrustHostnameVerifier(){
		if(allTrustHostnameVerifier== null){
			// Create all-trusting host name verifier
			allTrustHostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
		}
		return allTrustHostnameVerifier;
	}
	public static SSLSocketFactory  getAllTrustSocketFactory() throws NoSuchAlgorithmException, KeyManagementException{
		if(allTrustSocketFactory == null){
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					}
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					}
				}
			};
	
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			allTrustSocketFactory = sc.getSocketFactory();
		}
		return allTrustSocketFactory;
	}
	public static HttpURLConnection createConnectionWidthoutCertificateValidation(String surl) throws KeyManagementException, NoSuchAlgorithmException, IOException{
		URL url = new URL(surl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		if(HttpsURLConnection.class.isAssignableFrom(urlConnection.getClass())){
			((HttpsURLConnection)urlConnection).setSSLSocketFactory(getAllTrustSocketFactory());
			((HttpsURLConnection)urlConnection).setHostnameVerifier(getAllTrustHostnameVerifier());
		}
		return urlConnection;
	}
	
	/**
	 * If you are looking for any IP address that is valid for the local host 
	 * then you must check for special local host (e.g. 127.0.0.1) addresses as well as the ones assigned to any interfaces. For instance...
	 * REFERENCE:http://stackoverflow.com/questions/2406341/how-to-check-if-an-ip-address-is-the-local-host-on-a-multi-homed-system
	 * @param addr
	 * @return
	 */
	public static boolean isLocalIpAddress(InetAddress addr) {
		System.out.println("getHostName:"+addr.getHostName());
	    // Check if the address is a valid special local or loop back
	    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
	        return true;

	    // Check if the address is defined on any interface
	    try {
	        return NetworkInterface.getByInetAddress(addr) != null;
	    } catch (SocketException e) {
	        return false;
	    }
	}

	public static boolean isLocalIpAddress(String addr) {
		try {
			return isLocalIpAddress(InetAddress.getByName(addr));
		} catch (UnknownHostException e) {
		}
		return false;
	}

	public static void doSubmit(String url, RequestParameters params,RequestMethod post) throws SubmitException {
		doSubmit(url, params,post, true);
	}
	public static ByteArrayOutputStream doSubmit(String url, RequestParameters params,RequestMethod method,boolean certificateValidation) throws SubmitException{
		String content = "";
		if(params != null)
			content = params.getUrlParams();
		return doSubmit( url,  content, method, certificateValidation);
	}
	public static ByteArrayOutputStream doSubmit(String url, String content,RequestMethod method,boolean certificateValidation) throws SubmitException{
		System.out.println("[HttpUtil.doSubmit] invoking url:"+url);
		ByteArrayOutputStream rs = null;
		try{
				 
				HttpURLConnection conn = null;
				if(certificateValidation){
					URL siteUrl = new URL(url.replace("\n", ""));	
					conn = (HttpURLConnection) siteUrl.openConnection();
				}else
					conn = createConnectionWidthoutCertificateValidation(url.replace("\n", ""));
					
				
				conn.setRequestMethod(method.toString());
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
				conn.setRequestProperty("charset", "utf-8");
				conn.setRequestProperty("Content-Length", "" + content.getBytes().length);
				conn.setDoOutput(true);
				conn.setDoInput(true);
					
				System.out.println("[HttpUtil.doSubmit] write:"+content);		
				DataOutputStream out = new DataOutputStream(conn.getOutputStream());
				out.writeBytes(content);
				out.flush();
				out.close();
		
				System.out.println("[HttpUtil.doSubmit] reading bytes...");				
				rs = StreamUtil.readInputStream(conn.getInputStream());
				System.out.println("'"+new String(rs.toByteArray())+"'");
				conn.getInputStream().close();
		}catch(Exception e){
			throw new SubmitException(String.format("Error haciendo submit a: %s",url), e);
		}
		
		return rs;
	}
	public static ByteArrayOutputStream doSubmit(String url, RequestMethod get,boolean certificateValidation) throws SubmitException {
		int i = url.indexOf("?");
		RequestParameters params = new RequestParameters();
		if(i>0){
			String[] sparams = url.substring(i+1).split("&");
			String[] pair;
			for(String p:sparams){
				pair = new String[]{p.substring(0,p.indexOf("=")),p.substring(p.indexOf("=")+1)};
				if(pair.length == 2)
					params.addParameter(pair[0],pair[1]);
				else
					params.addParameter(pair[0],"");
			}
			url = url.substring(0,i);
		}
		return doSubmit(url, params, get,certificateValidation);
	}
	public static void main(String[] args) {
		RequestParameters params = new RequestParameters();
		params.addParameter("nro_orden","123456");
		params.addParameter("id_sesion","");
		params.addParameter("entidad","111");
		try {
			HttpUtil.doSubmit("http://test.pcfactory.cl/pos_xt_compra", params,RequestMethod.POST);
		} catch (SubmitException e) {
			e.printStackTrace();
		}
	}
}
