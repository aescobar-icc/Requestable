package beauty.requestable.core.wrappers;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import beauty.requestable.core.wrappers.WrapperServletOutputStream;

public class WrapperServletResponse extends HttpServletResponseWrapper {

	private WrapperServletOutputStream out;
	private CharArrayWriter charWriter;
    private PrintWriter printWriter;
    
    private int statusCode = 200;
    private String errorMsg = null;

	public WrapperServletResponse(HttpServletResponse response) {
		super(response);
	}
	
    public void close() throws IOException {
    	
        if (this.printWriter != null) {
            this.printWriter.close();
        }

        if (this.out != null) {
            this.out.close();
        }
    }
    
    //wrap errors
    @Override
    public void sendError(int sc) throws IOException {
    	this.statusCode = sc;
    }
    @Override
    public void sendError(int sc, String msg) throws IOException {
    	this.statusCode = sc;
    	this.errorMsg=msg;
    }
    @Override
    public int getStatus() {
    	return statusCode;
    }
    public String getErrorMsg() {
    	return errorMsg;
    }

	@Override
	public void flushBuffer() throws IOException {
		
        if(this.printWriter != null) {
            this.printWriter.flush();
        }

        IOException exception1 = null;
        try{
            if(this.out != null) {
                this.out.flush();
            }
        } catch(IOException e) {
            exception1 = e;
        }

        IOException exception2 = null;
        try {
            super.flushBuffer();
        } catch(IOException e){
            exception2 = e;
        }

        if(exception1 != null) throw exception1;
        if(exception2 != null) throw exception2;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
	    if (this.printWriter != null) {
	        throw new IllegalStateException("PrintWriter obtained already - cannot get OutputStream");
	    }
	    if (this.out == null) {
	        this.out = new WrapperServletOutputStream();
	    }
	    return this.out;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
	    if (this.printWriter == null && this.out != null) {
	        throw new IllegalStateException("OutputStream obtained already - cannot get PrintWriter");
	    }
	    if (this.printWriter == null) {
	        charWriter = new CharArrayWriter();
	        printWriter = new PrintWriter(charWriter);
	    }
	    return this.printWriter;
	}

	public byte[] getOutputStreamWrittenBytes(){
		if(out != null)
			return out.getBytes();
		return null;
	}
	public String getPrinterWrittenText(){
		if(charWriter != null)
			return charWriter.toString();
		return null;
	}
	

}
