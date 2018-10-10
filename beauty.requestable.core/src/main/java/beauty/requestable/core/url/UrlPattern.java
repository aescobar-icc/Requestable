package beauty.requestable.core.url;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPattern {
	
	private String uriRegex = "";
	private int priority = 1;
	private HashMap<String, Object> initData;

	public UrlPattern(String uriRegex) {
		this.uriRegex = uriRegex;
	}
	public UrlPattern(String uriRegex, int priority) {
		this.uriRegex = uriRegex;
		this.priority = priority;
	}

	

	public String getUriRegex() {
		return uriRegex;
	}
	public int getPriority() {
		return priority;
	}

	public void addData(String key,Object value) {
		if(initData == null) {
			initData = new HashMap<>();
		}
		initData.put(key, value);
	}
	public HashMap<String, Object> getInitData() {
		return initData;
	}
	
	/**
	 * Evalua si la uri suministrada hace match con el uriRegex definido
	 * @param uri
	 * @return si uri hace match devuelve el valor de priority, en otro caso devuelve cero.
	 */
	public int eval(String uri) {
		if(uriRegex != null && !uriRegex.trim().equals("")) {
			Pattern p = Pattern.compile(uriRegex);
			Matcher m = p.matcher(uri);
			if(m.find()) {
				return priority > 0? priority:0;
			}
		}
		return 0;
	}

}
