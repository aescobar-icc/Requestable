package beauty.requestable.util.http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RequestParameters implements Iterable<Param>{
	private List<Param> parameters = new ArrayList<Param>();
	private HashMap<String,Param> map = new HashMap<String,Param>();
	
	
	public void addParameter(String key, String value){		
		if(!map.containsKey(key)){
			Param p = new Param(key,value);
			parameters.add(p);
			map.put(key, p);
		}
		map.get(key).setValue(value);
	}
	/**
	 * Devuelve una cadena que contiene los parametros en formato url codificada en UTF-8
	 * @return
	 */
	public String getUrlParams(){
		StringBuilder urlParams = new StringBuilder();
		 
		for(Param p:parameters){
			try {
				urlParams.append(p.getEncodingPar("UTF-8")).append("&");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		int i = urlParams.length();
		if(i>0)
			urlParams.deleteCharAt(i-1);
		return urlParams.toString();
	}
	@Override
	public Iterator<Param> iterator() {
		return parameters.iterator();
	}
	
	@Override
	public String toString() {
		StringBuilder params = new StringBuilder();
		for(Param p:parameters){
			params.append(p.toString()).append("\n");
		}
		return params.toString();
	}
	
	public String toJSObject(){
		StringBuilder strb = new StringBuilder(); 
		strb.append("{");
		for(Param p:parameters){
			strb.append(p.getKey()).append(":").append(p.getValue()).append(",");
		} 
		strb.deleteCharAt(strb.length()-1);
		if(strb.length()>0)
			strb.append("}");
		return strb.toString();				
	}
	/**
	 * Retorna el parametro asociado al key especificado
	 * @param key
	 * @return
	 */
	public Param getParam(String key) {
		return map.get(key);
	}
}
