package beauty.requestable.core.flowcontrol;

import java.util.HashMap;

import beauty.requestable.core.annotations.RequestableClass;
import beauty.requestable.core.enums.FlowControlType;
import beauty.requestable.core.exceptions.RequestableSecurityException;

public class RequestableFlowControl {
	private FlowControlType flowType = FlowControlType.FORWARD;
	private RequestableClass requestableClass;
	private RequestableSecurityException exception;
	
	private HashMap<String, Object> params = new HashMap<String, Object>();
	
	private String redirectUri;
	
	

	public RequestableFlowControl(RequestableClass requestableClass, RequestableSecurityException exception) {
		super();
		this.requestableClass = requestableClass;
		this.exception = exception;
	}

	public RequestableClass getRequestableClass() {
		return requestableClass;
	}

	public RequestableSecurityException getException() {
		return exception;
	}

	public void addParam(String name,Object value) {
		params.put(name, value);
	}
	public HashMap<String, Object> getParams() {
		return params;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public FlowControlType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowControlType flowType) {
		this.flowType = flowType;
	}
	
	
}
