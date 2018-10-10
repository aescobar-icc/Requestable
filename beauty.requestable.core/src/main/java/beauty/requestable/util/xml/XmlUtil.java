package beauty.requestable.util.xml;

import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class XmlUtil {
	private static final HashMap<String, String> scapesXML = new HashMap<String, String>();
	static{
		scapesXML.put("\"",   "&quot;");
		scapesXML.put("'",   "&apos;");
		scapesXML.put("<",   "&lt;");
		scapesXML.put(">",   "&gt;");
		scapesXML.put("&",   "&amp;");
	}
	public static String scape(String value){
		if(value != null)
			for(String key:scapesXML.keySet()){
				value = value.replace(key, scapesXML.get(key));
			}		
		return value;
	}
	public static String nodeToString(Node doc){
	      try{
	         DOMSource domSource = new DOMSource(doc);
	         StringWriter writer = new StringWriter();
	         StreamResult result = new StreamResult(writer);
	         TransformerFactory tf = TransformerFactory.newInstance();
	         Transformer transformer = tf.newTransformer();
	         transformer.transform(domSource, result);
	         return writer.toString();
	      }
	      catch(TransformerException ex){
	         ex.printStackTrace();
	         return null;
	      }
	  }

}
