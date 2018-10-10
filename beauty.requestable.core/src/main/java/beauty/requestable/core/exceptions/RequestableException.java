package beauty.requestable.core.exceptions;

import java.sql.SQLException;

import beauty.requestable.util.reflection.UtilReflection;


public class RequestableException extends Exception {
	public static final int REQUEST_ALREADY_ON_PROCESS = 1000000001;
	/**
	 * 
	 */
	private static final long serialVersionUID = -1431402015452625836L;

	private int code;
	private String message;
	public RequestableException(String message) {
		super(message);
	}
	public RequestableException(String message,int code) {
		super(message);
		this.code = code;
	}
	public RequestableException(String message, Throwable cause) {
		super(message, cause);
		
		Throwable rootCause = UtilReflection.getRootCause(cause);
		if(RequestableException.class.isAssignableFrom(rootCause.getClass())){
			this.message = message+ " Detalle: "+ rootCause.getMessage();
		}else if(SQLException.class.isAssignableFrom(rootCause.getClass())){
			String causeMsg = rootCause.getMessage();
			if(causeMsg.startsWith("ERROR: [") && causeMsg.endsWith("]") )
				this.message = message+ " Detalle:"+causeMsg.substring(8, causeMsg.length()-1);
			else
				this.message = message+ " Detalle: Database Error";
		}else{
			this.message = message+ " Detalle:"+ rootCause.getMessage();
		}
	}
	public int getCode() {
		return code;
	}
	@Override
	public String getMessage() {
		if(this.message == null)
			return super.getMessage();
		return this.message;
	}
	

	
}
