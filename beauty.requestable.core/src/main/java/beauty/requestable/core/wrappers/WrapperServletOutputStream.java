package beauty.requestable.core.wrappers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class WrapperServletOutputStream extends ServletOutputStream {
	private ByteArrayOutputStream    out = null;

    public WrapperServletOutputStream()throws IOException {
        super();
        this.out = new ByteArrayOutputStream();
    }

    @Override
    public void close() throws IOException {
    	//System.out.println("ControllerServletOutputStream.close");
        this.out.close();
    }

    @Override
    public void flush() throws IOException {
    	//System.out.println("ControllerServletOutputStream.flush");
        this.out.flush();
    }

    @Override
    public void write(byte b[]) throws IOException {
    	//System.out.println("ControllerServletOutputStream.write byte b[] "+b);
        this.out.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
    	//System.out.println("ControllerServletOutputStream.write byte b[] off len"+b);
        this.out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
    	//System.out.println("ControllerServletOutputStream.write b"+b);
        this.out.write(b);
    }
    

	public byte[] getBytes(){
    	return out.toByteArray();
    }

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// TODO Auto-generated method stub
		
	}

}
