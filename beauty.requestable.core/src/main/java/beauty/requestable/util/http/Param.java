package beauty.requestable.util.http;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Param implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5387704355709709320L;
	private String key;
	private String value;
	
	public Param(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Devuelve el par en formato "key=value" con value formateado al encoding suministrado
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getEncodingPar(String encoding) throws UnsupportedEncodingException {
		
		return String.format("%s=%s", key,value!=null?URLEncoder.encode(value,encoding):"null");
	}
	
	@Override
	public String toString() {
		return String.format("%s=%s", key,value!=null?value:"null");
	}
}
