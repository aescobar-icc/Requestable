package beauty.requestable.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import beauty.requestable.util.reflection.UtilReflection;

public class UtilDOM {
	
	public static String getAttributeValue(Node node, String attributeName) {
		NamedNodeMap attr = node.getAttributes();
		for(int j=0;j < attr.getLength();j++){
			if(attr.item(j).getNodeName().equals(attributeName))
				return attr.item(j).getNodeValue();
		}
		return null;
	}
	public static HashMap<String, String>  getAllAttributes(Node node) {
		HashMap<String, String> attributes = new HashMap<String, String>();
		NamedNodeMap attr = node.getAttributes();
		for(int j=0;j < attr.getLength();j++){
			attributes.put(attr.item(j).getNodeName(), attr.item(j).getNodeValue());
		}
		return attributes;
	}
	public static String  getNamespace(Node node) {
		Element root = node.getOwnerDocument().getDocumentElement();
		HashMap<String, String> namespaces = getAllAttributes(root);
		String prefix = getPrefix(node.getNodeName());
		
		if(prefix == null)
			return namespaces.get("xmlns");
		
		return namespaces.get("xmlns:"+prefix);
	}
	public static String  getTargetnamespace(Node node) {
		Element root;
		if(node.getNodeType() == Node.DOCUMENT_NODE)
			root = ((Document)node).getDocumentElement();
		else
			root = node.getOwnerDocument().getDocumentElement();
		HashMap<String, String> namespaces = getAllAttributes(root);
		
		return namespaces.get("targetNamespace");
	}
	/**
	 * Search all direct child nodes that match with Node name, without consider its prefix name
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public static  List<Node> searchNode(Node parentNode,String nodeName){
		return searchNode(parentNode, nodeName,false,false);
	}
	public static  List<Node> searchNode(Node parentNode,String nodeName,String attributeName,String attributeValue){
		List<Node> nodes = searchNode(parentNode, nodeName,false,false);
		for(int i = nodes.size()-1; i >= 0 ; i--){
			if(!getAttributeValue(nodes.get(i), attributeName).equals(attributeValue))
				nodes.remove(i);
		}
		return nodes;
	}
	public static  List<Node> searchNode(Node parentNode,String nodeName,boolean considerPrefix,boolean recursive){
		if(parentNode.getNodeType() == Node.DOCUMENT_NODE)
			parentNode = ((Document)parentNode).getDocumentElement();
		
		NodeList  childNodes = parentNode.getChildNodes();
		List<Node> found = new ArrayList<Node>();
		
		for(int i=0;i < childNodes.getLength();i++){
			Node child = childNodes.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE){
				if(considerPrefix){
					if(child.getNodeName().equals(nodeName)){
						found.add(child);
					}
				}else{
					if(removePrefix(child.getNodeName()).equals(nodeName)){
						found.add(child);
					}
				}
				if(recursive)
					found.addAll(searchNode(child, nodeName, considerPrefix, recursive));
			}
		}
		return found;		
	}
	public static  String removePrefix(String value) {		
		return value.substring(value.indexOf(":")+1);
	}
	public static  String getPrefix(String value) {		
		try{
			return value.substring(0,value.indexOf(":") );
		}catch(Exception e){}
		return null;
	}

	public static String toString(Node node){
		return nodeToString(node, 0);
	}
	private static String nodeToString(Node node, int deep){

		if(node.getNodeType() == Node.DOCUMENT_NODE)
			node = ((Document)node).getDocumentElement();
		
		StringBuilder strb   = new StringBuilder();
		NodeList  childNodes = node.getChildNodes();
		String indent;
		
		for(int j=0;j < deep;j++)
			strb.append("\t");
		indent = strb.toString();
		
		if(node.getNodeType() == Node.ELEMENT_NODE ){
			
			strb.append("<").append(node.getNodeName());
			
			NamedNodeMap attr = node.getAttributes();
			for(int j=0;j < attr.getLength();j++){
				strb.append(" ").append(attr.item(j).getNodeName()).append("=\"").append(attr.item(j).getNodeValue()).append("\"");
			}
			if(childNodes.getLength() > 0){
				strb.append(">\n");
				for(int i=0;i < childNodes.getLength();i++){
					Node child = childNodes.item(i);
					if(child.getNodeType() == Node.ELEMENT_NODE || child.getNodeType() == Node.TEXT_NODE){
						strb.append(nodeToString(child,deep+1)).append("\n");
					}
				}
				strb.append(indent).append("</").append(node.getNodeName()).append(">");
			}else
				strb.append(" />");
		}else if(node.getNodeType() == Node.TEXT_NODE){
			strb.append(node.getTextContent());
		}
		
		return strb.toString();
	}
	public static Document createDocument(String uri) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		return db.parse(uri);
	}	
	public static Document createDocument(InputStream input) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
	
		return db.parse(input);
	}
	
	/**
	 * Load all the imports declarations contained in current node
	 * @param node
	 * @param locationRef
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<Document> loadImports(Node node,String relativeLocation) throws ParserConfigurationException, SAXException, IOException{
		List<Document> documents = new ArrayList<Document>();
		Document document;
		
		NodeList  childNodes = node.getChildNodes();
		for(int i=0;i < childNodes.getLength();i++){
			Node child = childNodes.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE){
				String nodeName = removePrefix(child.getNodeName());
				if(nodeName.equals("import") || nodeName.equals("include")){
					String location = UtilDOM.getAttributeValue(child, "location");
					if(location == null)
						location = UtilDOM.getAttributeValue(child, "schemaLocation");
					
					if(location != null){
						if(relativeLocation == null && !location.startsWith("http"))
							document = UtilDOM.createDocument(location);
						else
							document = UtilDOM.createDocument(relativeLocation+"/"+location);
						if(nodeName.equals("import"))
							documents.add(document);
						else if(getTargetnamespace(document.getDocumentElement()).equals(getTargetnamespace(child)))//the included files must all reference the same target namespace.
							documents.add(document);
						//busca imports del documento obtenido
						documents.addAll(loadImports(document.getDocumentElement(), relativeLocation));
					}
				}else //busca imports en los hijos del nodo actual
					documents.addAll(loadImports(child, relativeLocation));
			}
		}
		return documents;
	}
	
	/**
	 * Create a instance of org.w3c.dom.Document from a XML String
	 * @param xml
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document parseDocument(String xml) throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));

		return db.parse(is);
	}
	/**
	 * 
	 * @param parentNode
	 * @param type
	 * @return
	 * @throws DOMException
	 * @throws Exception
	 */
	public static <T> T parseToObject(Node parentNode,Class<T> type) throws DOMException, Exception{
		if(UtilReflection.isParameterizable(type)){
				 throw new RuntimeException("parseToObject not supported for "+type+", Generics are checked at compile-time for type-correctness. "
							+ "The generic type information is then removed in a process called type erasure. \n"
							+ "Use parseToObject(Node nodeField,Class<?> declaringClass,String fieldName) for ParameterizedType");
		}
		
		if(UtilReflection.isNativeType(type)){
			return UtilReflection.parseToNative(type, parentNode.getNodeValue());
		}else{
		
			T instance = type.newInstance();
			for(Field f:type.getDeclaredFields()){
				Node nodeField = null;
				try{
					nodeField = searchNode(parentNode,f.getName()).get(0);
				}catch(IndexOutOfBoundsException e){}
				if(nodeField != null){
					UtilReflection.setFieldValue(f, instance, parseToObject(nodeField, f));
				}
			}
			return instance;
		}		
	}

	public static Object parseToObject(Node nodeField,Class<?> declaringClass,String fieldName) throws DOMException, Exception{
		Field field = declaringClass.getField(fieldName);
		return parseToObject(nodeField, field);
	}
	public static Object parseToObject(Node nodeField,Field field) throws DOMException, Exception{
		if(UtilReflection.isNativeType(field.getType())){						
			String value = nodeField.getTextContent();
			return UtilReflection.parseToNative(field.getType(),value);
		}else if(List.class.isAssignableFrom(field.getType())){
			Type parameterType = UtilReflection.getParameterTypes(field)[0];
			
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) UtilReflection.newInstance(field.getType());
			if(list == null)
				list = new ArrayList<Object>();
			NodeList fieldChields = nodeField.getChildNodes();
			Object obj;
			for(int i=0; i < fieldChields.getLength();i++){
				if(fieldChields.item(i).getNodeType() == Node.ELEMENT_NODE){
					if(parameterType instanceof ParameterizedType)
						obj = parseToList(fieldChields.item(i), (ParameterizedType) parameterType);
					else
						obj = parseToObject(fieldChields.item(i), (Class<?>) parameterType);
					list.add(obj);
				}
			}
			return list;
		}else{
			return parseToObject(nodeField,  field.getType());
		}
	
	}
	/**
	 * Used to decode List of List
	 * @param parentNode
	 * @param type
	 * @return
	 * @throws DOMException
	 * @throws Exception
	 */
	private static List<Object> parseToList(Node parentNode, ParameterizedType type) throws DOMException, Exception {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) UtilReflection.newInstance(type);
		if(list == null)
			list = new ArrayList<Object>();
		
		NodeList childs = parentNode.getChildNodes();
		Type parameterType = type.getActualTypeArguments()[0];
		
		for(int i=0; i < childs.getLength();i++){
			if(childs.item(i).getNodeType() == Node.ELEMENT_NODE){
				Object obj;
				if(parameterType instanceof ParameterizedType)
					obj = parseToList(childs.item(i), (ParameterizedType) parameterType);
				else 
					obj = parseToObject(childs.item(i), (Class<?>) parameterType);
				if(obj != null)
					list.add(obj);
			}
			
		}
		
		return list;
	}
 	public static void printDifferences(Node node1,Node node2){
 		if(node1.getNodeType() == Node.DOCUMENT_NODE)
 			node1 = ((Document)node1).getDocumentElement();
 		
 		if(node2.getNodeType() == Node.DOCUMENT_NODE)
 			node2 = ((Document)node2).getDocumentElement();
 		
 		if(node1.getNodeType() == Node.ELEMENT_NODE){
	 		NamedNodeMap attrs1 = node1.getAttributes();
			NamedNodeMap attrs2 = node2.getAttributes();
			Node child1,child2;
			int i,j;
			for(i=0;i < attrs1.getLength();i++){
				child1 = attrs1.item(i);
	 			for(j=0;j < attrs2.getLength();j++){
	 				child2 = attrs2.item(j);
					if(child1.getNodeName().equals(child2.getNodeName())){
						if(!child1.getNodeValue().equals(child2.getNodeValue()))
							System.out.println(String.format("%s -- %s \tvalue1 = %s \tvalue2 = %s",node1.getNodeName(), child1.getNodeName(),child1.getNodeValue(),child2.getNodeValue()));
						break;
					}
	 			}
	 			if(j == attrs2.getLength()){
					//System.out.println(String.format("%s -- %s \tvalue1 = %s \t value2 = !not present",node1.getNodeName(), child1.getNodeName(),child1.getNodeValue()));
	 			}
			}
	
			NodeList childs1 = node1.getChildNodes();
			NodeList childs2 = node2.getChildNodes();
			for(i=0;i<childs1.getLength();i++){
	            child1 = childs1.item(i);
	            child2 = childs2.item(i);
	            printDifferences(child1, child2);
	        }
 		}
		
 	}
 	public static void main(String[] args) {
		try {
			Document doc1 = createDocument("/Users/aescobar/Documents/workspace/branch/Sphinx/WebContent/WEB-INF/xsl/modulos/caja/npp_gen.xml");
			Document doc2 = createDocument("/Users/aescobar/Documents/workspace/branch/Sphinx/WebContent/WEB-INF/xsl/modulos/caja/nppa.xml");
			printDifferences(doc1, doc2);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/*public ArrayList<WebServiceClient> a = new ArrayList<WebServiceClient>();
	public static void main(String[] args) {
		String xml = "<object>"
					+ "		<arboles>"
					+ "			<arbol>"
					+ "				<node>"
					+ "					<index>1</index>"
					+ "					<name>node1</name>"
					+ "				</node>"
					+ "				<node>"
					+ "					<index>2</index>"
					+ "					<name>node2</name>"
					+ "				</node>"
					+ "			</arbol>"
					+ "			<arbol>"
					+ "				<node>"
					+ "					<index>3</index>"
					+ "					<name>node3</name>"
					+ "				</node>"
					+ "				<node>"
					+ "					<index>4</index>"
					+ "					<name>node4</name>"
					+ "				</node>"
					+ "			</arbol>"
					+ "		</arboles>"
					+ "			<arbol>"
					+ "				<node>"
					+ "					<index>5</index>"
					+ "					<name>node5</name>"
					+ "				</node>"
					+ "				<node>"
					+ "					<index>6</index>"
					+ "					<name>node6</name>"
					+ "				</node>"
					+ "			</arbol>"
					+ "</object>";
		try {
			Document doc = parseDocument(xml);
			Node node = searchNode(doc.getDocumentElement(), "arboles").get(0);
			node = searchNode(node, "arbol").get(0);
			//System.out.println(toString(node));

			//Object arboles = parseToObject(doc.getDocumentElement(), Arboles.class);
			//Object arboles = parseToObject(node, Arboles.class.getDeclaredField("arbol"));
			
			//System.out.println(arboles);
			
			//parseToObject(node,Arboles.class.getDeclaredField("arboles").getType());
		} catch (Throwable e) {
			e.printStackTrace();
		} 
	}*/
	
}
