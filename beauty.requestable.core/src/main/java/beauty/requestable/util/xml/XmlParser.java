package beauty.requestable.util.xml;


import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import beauty.requestable.util.format.UtilStringFormat;
import beauty.requestable.util.reflection.UtilReflection;

public class XmlParser {

	public static Node parseObjectToNode(Object obj){
		Document doc = createDocument();
		Node objXML = doc.createElement(UtilStringFormat.camelToUnder(obj.getClass().getSimpleName()));
		
		if(Collection.class.isAssignableFrom(obj.getClass())){
			for (Object lstObj :(Collection<?>) obj) {
				objXML.appendChild(doc.importNode(parseObjectToNode(lstObj), true));
			}
		}else if(obj.getClass().isPrimitive() || UtilReflection.isPosibleNativeType(obj.getClass())){
			objXML.setTextContent(String.valueOf(obj));
		}else
			createChild(obj, objXML);
		
		return objXML;
	}
	private static void createChild(Object obj,Node parent){
		if(obj == null)
			return;
		Node child;
		List<Field> fields = UtilReflection.getAllFields(obj.getClass());//obj.getClass().getDeclaredFields();
		for(Field field:fields){
			if(field.getName().equals("serialVersionUID"))
				continue;
			Document doc = parent.getOwnerDocument();
			child = doc.createElement(UtilStringFormat.camelToUnder(field.getName()));			
			try {

				if(Collection.class.isAssignableFrom(field.getType())){
					List<?> list = (List<?>) UtilReflection.getFieldValue(field, obj);
					if(list != null){
						for (Object lstObj : list) {
							child.appendChild(doc.importNode(parseObjectToNode(lstObj), true));
						}
					}
				}
				else if(field.getType().isPrimitive() || UtilReflection.isPosibleNativeType(field.getType())){
					String value = String.valueOf(UtilReflection.getFieldValue(field, obj));
					if(value == null || value.equals("null"))
						value = "";
					child.setTextContent(value);	
					
				}else{
					//child.appendChild(doc.importNode(parseToXML(field.get(obj),false), true));
					createChild(UtilReflection.getFieldValue(field, obj), child);					
				}
			} catch (Exception e){					
				System.err.println("Error procesing field:"+field.getName()+" accesible:"+field.isAccessible());
				e.printStackTrace();
			}
			parent.appendChild(child);
		}
	}
	public static String parseObjectToXML(Object obj){		
		StringBuilder objXML = new StringBuilder();
		parseObjectToXML(obj, objXML);
		return UtilStringFormat.camelToUnder(objXML.toString());
	}

	private static void parseObjectToXML(Object obj, StringBuilder objXML){
		
		objXML.append("\n<").append(obj.getClass().getSimpleName()).append(">");
		if(Collection.class.isAssignableFrom(obj.getClass())){
			for (Object lstObj :(Collection<?>) obj) {
				parseObjectToXML(lstObj,objXML);
			}
		}else
			createChild(obj, objXML);
		objXML.append("</").append(obj.getClass().getSimpleName()).append(">\n");
	}
	private static void createChild(Object obj, StringBuilder objXML) {
		if(obj == null)
			return;
		List<Field> fields = UtilReflection.getAllFields(obj.getClass());//obj.getClass().getDeclaredFields();
		for(Field field:fields){
			if(field.getName().equals("serialVersionUID"))
				continue;
			objXML.append("\n<").append(field.getName()).append(">");			
			try {

				if(Collection.class.isAssignableFrom(field.getType())){
					List<?> list = (List<?>) UtilReflection.getFieldValue(field, obj);
					if(list != null){
						for (Object lstObj : list) {
							parseObjectToXML(lstObj,objXML);
						}
					}
				}
				else if(field.getType().isPrimitive() || UtilReflection.isPosibleNativeType(field.getType())){						
					objXML.append(String.valueOf(UtilReflection.getFieldValue(field, obj)));						
				}else{
					//child.appendChild(doc.importNode(parseToXML(field.get(obj),false), true));
					createChild(UtilReflection.getFieldValue(field, obj), objXML);					
				}
			} catch (Exception e){					
				System.err.println("Error procesing field:"+field.getName()+" accesible:"+field.isAccessible());
				e.printStackTrace();
			}
			objXML.append("</").append(field.getName()).append(">\n");		
		}
	
	}
	public static Document createDocument(){

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
	      DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			return builder.newDocument(); 
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String parseNode(Node node){
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(buffer));
			return  buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	 public static String getStringFromDocument(Document doc)
	  {
	      try
	      {
	         DOMSource domSource = new DOMSource(doc);
	         StringWriter writer = new StringWriter();
	         StreamResult result = new StreamResult(writer);
	         TransformerFactory tf = TransformerFactory.newInstance();
	         Transformer transformer = tf.newTransformer();
	         transformer.transform(domSource, result);
	         return writer.toString();
	      }
	      catch(TransformerException ex)
	      {
	         ex.printStackTrace();
	         return null;
	      }
	  } 

}
