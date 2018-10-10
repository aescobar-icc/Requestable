package beauty.requestable.core;

import java.util.ArrayList;
import java.util.List;

import beauty.requestable.util.serialize.JSONUtil;

/**
 * This class implements the response struct for an requestable service
 * @author aescobar
 *
 */
public class RequestableResponse {
	private Object			data;
	private List<String>	messages;
	private boolean			isOk = false;
	private int				resultCode = 0;
	
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void addMessage(String message) {
		if(this.messages == null)
			this.messages = new ArrayList<String>();
		this.messages.add(message);
	}
	public int getResultCode() {
		return resultCode;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	public String parseJson(){
		return JSONUtil.encodeJsonString(this);
	}
	public boolean isOk() {
		return isOk;
	}
	public void setOk(boolean isOk) {
		this.isOk = isOk;
	}
	

}
