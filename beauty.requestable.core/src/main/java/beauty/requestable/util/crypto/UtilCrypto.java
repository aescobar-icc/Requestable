package beauty.requestable.util.crypto;


import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import beauty.requestable.util.file.FileUtil;
import beauty.requestable.util.xml.UtilDOM;

public class UtilCrypto {
	private static final  String ALGORITHM 		= "AES";
	private static final  String TRANSFORMATION = "AES/CBC/PKCS5PADDING";
	private static final  byte[] KEY_ONE		= "1eht79.bdg8o5vgu".getBytes();
	private static final  byte[] KEY_TWO		= "48ks05mb7ths4qdk".getBytes();
	
	 public static String encrypt(String value) {
	        try {
	            IvParameterSpec iv = new IvParameterSpec(KEY_TWO);
	            SecretKeySpec skeySpec = new SecretKeySpec(KEY_ONE,ALGORITHM);
	            
	            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
	            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
	            byte[] encrypted = cipher.doFinal(value.getBytes());
	          //  Encoder base64Encoder = Base64.getEncoder();
	           // System.out.println("encrypted string:"+ base64Encoder.encodeToString(encrypted));
	            return new String(Base64.encode(encrypted),"UTF-8");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return null;
	    }

	    public static String decrypt(String encrypted) {
	        try {
	            IvParameterSpec iv = new IvParameterSpec(KEY_TWO);
	            SecretKeySpec skeySpec = new SecretKeySpec(KEY_ONE,ALGORITHM);
	            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
	            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
	            //Decoder base64Dencoder = Base64.getDecoder();
	            byte[] original = cipher.doFinal(Base64.decode(encrypted.getBytes()));

	            return new String(original);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return null;
	    }

	    public static void main(String[] args) {
	        System.out.println("-------------ServiceRunner: service.xml-----------------");
	    	String fileContent = "";
			try {
				fileContent = UtilDOM.toString(UtilDOM.createDocument("../ServiceRunner/resources/service.xml"));
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
	    	String encriptFile = encrypt(fileContent);
	        System.out.println(encriptFile);
	        System.out.println("------------------------------------------");
	        System.out.println(decrypt(encriptFile));

	        System.out.println("------------- ServiceRunner: db.properties-----------------");
	        try {
				fileContent = new String(FileUtil.readFile("../ServiceRunner/resources/db.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	encriptFile = encrypt(fileContent);
	        System.out.println(encriptFile);
	        System.out.println("------------------------------------------");
	        System.out.println(decrypt(encriptFile));
	        

	        System.out.println("-------------ServiceRunnerClient: runner.properties-----------------");
	        try {
				fileContent = new String(FileUtil.readFile("../ServiceRunnerClient/resources/runner.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	    	encriptFile = encrypt(fileContent);
	        System.out.println(encriptFile);
	        System.out.println("------------------------------------------");
	        System.out.println(decrypt(encriptFile));
	        
	        
	        System.out.println("------------- ReportMaker: reportdb.properties-----------------");
	        try {
				fileContent = new String(FileUtil.readFile("../ReportMaker/resources/properties/reportdb.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	    	encriptFile = encrypt(fileContent);
	        System.out.println(encriptFile);
	        System.out.println("------------------------------------------");
	        System.out.println(decrypt(encriptFile));
	        
	        
	    }
}
