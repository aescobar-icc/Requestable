package beauty.requestable.core.exceptions;

public class RequestableSecurityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;

	public RequestableSecurityException() {
	}

	public RequestableSecurityException(String message) {
		super(message);
	}
	public RequestableSecurityException(String message,int code) {
		super(message);
		this.code = code;
	}

	public RequestableSecurityException(Throwable cause) {
		super(cause);
	}

	public RequestableSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestableSecurityException(String message, Throwable cause,boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public int getCode() {
		return code;
	}

}
