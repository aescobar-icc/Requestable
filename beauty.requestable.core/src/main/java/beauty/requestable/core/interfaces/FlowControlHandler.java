package beauty.requestable.core.interfaces;

import beauty.requestable.core.flowcontrol.RequestableFlowControl;

public interface FlowControlHandler {
	void onSecurityException(RequestableFlowControl flowControl);
}
