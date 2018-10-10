package beauty.requestable.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beauty.requestable.util.bytes.BytesUtil;
import beauty.requestable.util.stream.StreamUtil;

public class HttpProtocol {
	private enum ConnectionStatus{CREATE_REQUEST_HEADERS,READ_RESPONSE_HEADERS,READ_RESPONSE_BODY,CREATE_REQUEST_BODY};
	private ConnectionStatus currentStatus = ConnectionStatus.CREATE_REQUEST_HEADERS;

	private static final byte[] DELIMITER = "\r\n".getBytes();
	private static final byte[] END_DELIMITER = "\r\n\r\n".getBytes();
	private static final byte[] HEADER_DELIMITER = ": ".getBytes();
	private static final byte[] SPACE = " ".getBytes();
	
	private String 			host="";
	private String 			uri="/";
	private int 			port=80;
	
	private byte[] content;
	private HTTPMethod method;

	private int responseCode = 0;
	Map<String, List<String>> responseHeaders; 
	

	private Map<String, String> cookies = new HashMap<String, String>();
	
	
	public byte[] processConnection(InputStream inputStream) throws IOException{
		switch(currentStatus){
			case CREATE_REQUEST_HEADERS:
				currentStatus = ConnectionStatus.READ_RESPONSE_HEADERS;
				System.out.println("-- CREATE_REQUEST_HEADERS");
				return createRequest(HTTPMethod.GET,null);
			case READ_RESPONSE_HEADERS:
				System.out.println("-- READ_RESPONSE_HEADERS");
				responseCode 	= getResponseCode(inputStream);
				responseHeaders = getHeaders(inputStream);
				if(isRedirect(responseCode)){
					System.out.println("-- SERVER RESPONSE REDIRECT to:"+responseHeaders.get("Location").get(0));
					setURL(responseHeaders.get("Location").get(0));
					ByteArrayOutputStream baos;
					if(responseHeaders.containsKey("Content-Length")){
						int length =Integer.parseInt(responseHeaders.get("Content-Length").get(0));
						baos = StreamUtil.readInputStream(inputStream,length);
					}else{
						baos = StreamUtil.readInputStream(inputStream,END_DELIMITER,true);
					}
					System.out.println("READING EXTRA BYTES");
					System.out.println(new String(baos.toByteArray())+" length:"+baos.toByteArray().length);
					currentStatus = ConnectionStatus.CREATE_REQUEST_HEADERS;
					return "".getBytes();
				}else{ 
					currentStatus = ConnectionStatus.READ_RESPONSE_BODY;
					return "".getBytes();
				}
			case READ_RESPONSE_BODY:
				System.out.println("-- READ_RESPONSE_BODY");
				if(responseCode == HTTP_OK){
					int length =Integer.parseInt(responseHeaders.get("Content-Length").get(0));
					ByteArrayOutputStream baos = StreamUtil.readInputStream(inputStream,length);
					System.out.println(new String(baos.toByteArray())+" length:"+baos.toByteArray().length);
				}
				currentStatus = ConnectionStatus.CREATE_REQUEST_BODY;
				return "".getBytes();
			case CREATE_REQUEST_BODY:
				if(content != null){
					System.out.println("-- CREATE_REQUEST_BODY");
					currentStatus = ConnectionStatus.READ_RESPONSE_HEADERS;
					System.out.println("-- CREATING CLIENT REQUEST");
					byte[] req = createRequest(method,content);
					content = null;
					return req;
				}
				break;
			default:
				break;
		
		}
		
		return null;
	}
	private boolean isRedirect(int code) {
		 return HTTP_MULT_CHOICE == code || 
				 HTTP_MOVED_PERM == code ||  
				 HTTP_MOVED_TEMP == code || 
				 HTTP_SEE_OTHER == code || 
				 HTTP_NOT_MODIFIED == code || 
				 HTTP_USE_PROXY == code;
	}
	private Map<String, List<String>> getHeaders(InputStream inputStream) throws IOException {
		String name,value;
		int start,end;
		byte[] hdrs = StreamUtil.readInputStream(inputStream, END_DELIMITER, true).toByteArray();
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		for(start=0;start<hdrs.length - END_DELIMITER.length;start = end +DELIMITER.length){
			end	= BytesUtil.indexOf(start, hdrs, HEADER_DELIMITER);
			name = new String(BytesUtil.subArray(start, end, hdrs));
			
			start = end + HEADER_DELIMITER.length;
			end	= BytesUtil.indexOf(start, hdrs, DELIMITER);
			value = new String(BytesUtil.subArray(start, end, hdrs));
			System.out.println(String.format("%s : %s", name,value));
			
			if(headers.containsKey(name))
				headers.get(name).add(value);
			else{
				List<String> values = new ArrayList<String>();
				values.add(value);
				headers.put(name, values);
			}
			
			if("Set-Cookie".equals(name)){
				setCookie(value);
			}
			
		}
		return headers;
	}
	private void setCookie(String cookie) {
		cookie = cookie.substring(0, cookie.indexOf(";"));
		String[] par = cookie.split("=");
		cookies.put(par[0], par[1]);
	}
	private int getResponseCode(InputStream inputStream) throws IOException {
		byte[] responseCode = StreamUtil.readInputStream(inputStream, DELIMITER, true).toByteArray();
		if(responseCode.length > 0){
			System.out.println(new String(responseCode));
			int start		= BytesUtil.indexOf(responseCode, SPACE) + 1;
			int end			= BytesUtil.indexOf(start, responseCode, SPACE);
			responseCode	= BytesUtil.subArray(start, end, responseCode);
			
			return Integer.parseInt(new String(responseCode));
		}
		return -1;
	}
	private byte[] createRequest(HTTPMethod method,byte[] content) {
		StringBuilder builder = new StringBuilder();
	    if(method == null)
	    	method = HTTPMethod.GET;
		builder.append(method).append(String.format(" %s HTTP/1.1",uri)).append(new String(DELIMITER));
		builder.append("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8").append(new String(DELIMITER));
		//builder.append("Accept-Encoding:gzip, deflate, sdch").append(new String(DELIMITER));
		builder.append("Accept-Language:en-US,en;q=0.8,es;q=0.6").append(new String(DELIMITER));
		builder.append("Cache-Control:max-age=0").append(new String(DELIMITER));
		builder.append("Connection:keep-alive").append(new String(DELIMITER));
		builder.append(String.format("Host:%s:%s",host,port)).append(new String(DELIMITER));
		builder.append("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36").append(new String(DELIMITER));
		
		if(cookies.size() > 0){
			builder.append("Cookie: ");
			for(String name:cookies.keySet()){
				builder.append(name).append("=").append(cookies.get(name)).append("; ");
			}
			builder.append(new String(DELIMITER));
		}
		if(content != null){
			builder.append("Content-Type: application/x-www-form-urlencoded").append(new String(DELIMITER));
			builder.append("Content-Length: ").append(content.length).append(new String(DELIMITER));
			builder.append(new String(DELIMITER));
			for(byte b : content) {
		        builder.append(String.format("%c", b));
		    }
		}
		builder.append(new String(END_DELIMITER));
		System.out.println(builder);
	    return builder.toString().getBytes();
	}
	public void setURL(String url){
		int i=0;
		if(url.indexOf("http://") >= 0)
			i= 7;
		
		if(url.indexOf("/", i) > 0){
			host = url.substring(i, url.indexOf("/", i));
			uri  = url.substring(url.indexOf("/", i)); 		
		}else{
			host = url.substring(i);
		}
		if((i = host.indexOf(":")) > 0){
			port = Integer.parseInt(host.substring(i+1));
			host = host.substring(0,i);
		}

	}
	public String getHost(){
		return host;
	}
	public String getUri(){
		return uri;
	}
	public int getPort(){
		return port;
	}
	
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}

	public HTTPMethod getMethod() {
		return method;
	}
	public void setMethod(HTTPMethod method) {
		this.method = method;
	}

	/**
     * HTTP Status-Code 200: OK.
     */
    public static final int HTTP_OK = 200;

    /**
     * HTTP Status-Code 201: Created.
     */
    public static final int HTTP_CREATED = 201;

    /**
     * HTTP Status-Code 202: Accepted.
     */
    public static final int HTTP_ACCEPTED = 202;

    /**
     * HTTP Status-Code 203: Non-Authoritative Information.
     */
    public static final int HTTP_NOT_AUTHORITATIVE = 203;

    /**
     * HTTP Status-Code 204: No Content.
     */
    public static final int HTTP_NO_CONTENT = 204;

    /**
     * HTTP Status-Code 205: Reset Content.
     */
    public static final int HTTP_RESET = 205;

    /**
     * HTTP Status-Code 206: Partial Content.
     */
    public static final int HTTP_PARTIAL = 206;

    /* 3XX: relocation/redirect */

    /**
     * HTTP Status-Code 300: Multiple Choices.
     */
    public static final int HTTP_MULT_CHOICE = 300;

    /**
     * HTTP Status-Code 301: Moved Permanently.
     */
    public static final int HTTP_MOVED_PERM = 301;

    /**
     * HTTP Status-Code 302: Temporary Redirect.
     */
    public static final int HTTP_MOVED_TEMP = 302;

    /**
     * HTTP Status-Code 303: See Other.
     */
    public static final int HTTP_SEE_OTHER = 303;

    /**
     * HTTP Status-Code 304: Not Modified.
     */
    public static final int HTTP_NOT_MODIFIED = 304;

    /**
     * HTTP Status-Code 305: Use Proxy.
     */
    public static final int HTTP_USE_PROXY = 305;

    /* 4XX: client error */

    /**
     * HTTP Status-Code 400: Bad Request.
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * HTTP Status-Code 401: Unauthorized.
     */
    public static final int HTTP_UNAUTHORIZED = 401;

    /**
     * HTTP Status-Code 402: Payment Required.
     */
    public static final int HTTP_PAYMENT_REQUIRED = 402;

    /**
     * HTTP Status-Code 403: Forbidden.
     */
    public static final int HTTP_FORBIDDEN = 403;

    /**
     * HTTP Status-Code 404: Not Found.
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * HTTP Status-Code 405: Method Not Allowed.
     */
    public static final int HTTP_BAD_METHOD = 405;

    /**
     * HTTP Status-Code 406: Not Acceptable.
     */
    public static final int HTTP_NOT_ACCEPTABLE = 406;

    /**
     * HTTP Status-Code 407: Proxy Authentication Required.
     */
    public static final int HTTP_PROXY_AUTH = 407;

    /**
     * HTTP Status-Code 408: Request Time-Out.
     */
    public static final int HTTP_CLIENT_TIMEOUT = 408;

    /**
     * HTTP Status-Code 409: Conflict.
     */
    public static final int HTTP_CONFLICT = 409;

    /**
     * HTTP Status-Code 410: Gone.
     */
    public static final int HTTP_GONE = 410;

    /**
     * HTTP Status-Code 411: Length Required.
     */
    public static final int HTTP_LENGTH_REQUIRED = 411;

    /**
     * HTTP Status-Code 412: Precondition Failed.
     */
    public static final int HTTP_PRECON_FAILED = 412;

    /**
     * HTTP Status-Code 413: Request Entity Too Large.
     */
    public static final int HTTP_ENTITY_TOO_LARGE = 413;

    /**
     * HTTP Status-Code 414: Request-URI Too Large.
     */
    public static final int HTTP_REQ_TOO_LONG = 414;

    /**
     * HTTP Status-Code 415: Unsupported Media Type.
     */
    public static final int HTTP_UNSUPPORTED_TYPE = 415;

    /* 5XX: server error */

    /**
     * HTTP Status-Code 500: Internal Server Error.
     * @deprecated   it is misplaced and shouldn't have existed.
     */
    @Deprecated
    public static final int HTTP_SERVER_ERROR = 500;

    /**
     * HTTP Status-Code 500: Internal Server Error.
     */
    public static final int HTTP_INTERNAL_ERROR = 500;

    /**
     * HTTP Status-Code 501: Not Implemented.
     */
    public static final int HTTP_NOT_IMPLEMENTED = 501;

    /**
     * HTTP Status-Code 502: Bad Gateway.
     */
    public static final int HTTP_BAD_GATEWAY = 502;

    /**
     * HTTP Status-Code 503: Service Unavailable.
     */
    public static final int HTTP_UNAVAILABLE = 503;

    /**
     * HTTP Status-Code 504: Gateway Timeout.
     */
    public static final int HTTP_GATEWAY_TIMEOUT = 504;

    /**
     * HTTP Status-Code 505: HTTP Version Not Supported.
     */
    public static final int HTTP_VERSION = 505;


	
}
