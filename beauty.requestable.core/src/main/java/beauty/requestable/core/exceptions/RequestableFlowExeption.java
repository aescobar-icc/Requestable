package beauty.requestable.core.exceptions;

import beauty.requestable.core.flowcontrol.RequestableFlowControl;

public class RequestableFlowExeption extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RequestableFlowControl flowControl;
	public RequestableFlowExeption(RequestableFlowControl flowControl) {
		this.flowControl = flowControl;
	}
	public RequestableFlowControl getFlowControl() {
		return flowControl;
	}

}
