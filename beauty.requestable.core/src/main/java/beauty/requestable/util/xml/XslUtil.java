package beauty.requestable.util.xml;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import beauty.requestable.util.reflection.UtilReflection;
import beauty.requestable.util.serialize.JSONUtil;

/**
 * 
 * @author aescobar
 *
 */
public class XslUtil {
	public static Node creaTagPaginacion(int idPaginador,int registrosPorPagina, int cantidadTotalRegistros){
		return creaTagPaginacion(idPaginador, registrosPorPagina, cantidadTotalRegistros, 1);
	}
	
	public static Node creaTagPaginacion(int idPaginador,int registrosPorPagina, int cantidadTotalRegistros,int currentPage){
		/*
		 <div id="paginador_" indexPaginador="1" currentPage="1" totalPage="20" start="1" end="5">
		  </div>
		 * */
		int maxPages = (int)Math.ceil((double)cantidadTotalRegistros/registrosPorPagina);
		int cBtns = 1;
		
		if(maxPages<1)
			maxPages = 1;
		else if(maxPages>5){
				cBtns = 5;
		}else
			cBtns = maxPages;		
				
		Document doc = XmlParser.createDocument();
		Element root = doc.createElement("info_pag");
		root.setAttribute("indexPaginador", String.valueOf(idPaginador));
		root.setAttribute("currentPage", String.valueOf(currentPage));
		root.setAttribute("totalPage",		String.valueOf(maxPages));
		
		if(currentPage > 5)
			root.setAttribute("start",	String.valueOf(currentPage-4));
		else
			root.setAttribute("start",	"1");
		
		if(currentPage > cBtns)
			root.setAttribute("end",	String.valueOf(currentPage));	
		else
			root.setAttribute("end",	String.valueOf(cBtns));	
		
		return root;
		
	}
	public static Node createActionResult(Object obj){
		Document doc = XmlParser.createDocument();
		Element responce = doc.createElement("action_responce");
		Node objetoXml = XmlParser.parseObjectToNode(obj);
		responce.appendChild(doc.importNode(objetoXml,true));
		return responce;
	}
	public static void makeResult(Document xmlDoc,String result, String message) {
		HashMap<String, String> resp = new HashMap<String, String>();
		resp.put("resp", result);
		resp.put("message",message);
		
		Node objetoXml = XmlParser.parseObjectToNode(resp) ;
		Element actionResponce = xmlDoc.createElement("action_responce");
		objetoXml = xmlDoc.importNode(objetoXml,true);
		actionResponce.appendChild(objetoXml);		
		xmlDoc.getDocumentElement().appendChild(actionResponce);	
		
	}
	public static void createSelectOptions(Document xmlDoc,String idSelect,String fieldValue,String fieldText,int selectedIndex,List<?> options){
		StringBuilder strb = new StringBuilder();
		strb.append("<").append(idSelect).append(">");
		int i=0;
		for(Object obj:options){
			try {
				if(i == selectedIndex)
					strb.append("<option value=\"").append(UtilReflection.getFieldValue(fieldValue, obj)).append("\" selected=\"selected\">").append(XmlUtil.scape((String) UtilReflection.getFieldValue(fieldText, obj))).append("</option>");
				else
					strb.append("<option value=\"").append(UtilReflection.getFieldValue(fieldValue, obj)).append("\">").append(XmlUtil.scape((String) UtilReflection.getFieldValue(fieldText, obj))).append("</option>");
			} catch (Exception e) {
				e.printStackTrace();
			} 
			i++;
		}
		strb.append("</").append(idSelect).append(">");
		
		Element actionResponce = xmlDoc.createElement("action_responce");
		xmlDoc.getDocumentElement().appendChild(actionResponce);		
		actionResponce.setTextContent(strb.toString());
	}
	public static void createSelectOptions(Document xmlDoc,String idSelect,String fieldValue,String fieldText,String defaultValue,String defaultText,List<?> options){
		StringBuilder strb = new StringBuilder();
		strb.append("<").append(idSelect).append(">");
		if(defaultValue!= null && defaultText!= null)
			strb.append("<option value=\"").append(defaultValue).append("\" selected=\"selected\">").append(defaultText).append("</option>");
		for(Object obj:options){
			try {
				strb.append("<option value=\"").append(UtilReflection.getFieldValue(fieldValue, obj)).append("\">").append(XmlUtil.scape((String) UtilReflection.getFieldValue(fieldText, obj))).append("</option>");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		strb.append("</").append(idSelect).append(">");
		
		Element actionResponce = xmlDoc.createElement("action_responce");
		xmlDoc.getDocumentElement().appendChild(actionResponce);		
		actionResponce.setTextContent(strb.toString());
	}
	/**
	 * Crea una tabla que muestra los campos del objecto que se han especificaco en 'includeFields'
	 * @param xmlDoc
	 * @param idElement
	 * @param title
	 * @param obj
	 * @param includeFields
	 */
	public static void renderObjectOnTable(Document xmlDoc,String idElement,String title,Object obj,List<String> includeFields){
		List<Field> fields = new ArrayList<Field>(includeFields.size());
		HashMap<String, String> alias = new HashMap<String, String>();
		for(String name:includeFields){
			for(Field field:obj.getClass().getDeclaredFields()){
				if(name.contains(":")){
					String[] values = name.split(":");
					name = values[0];
					alias.put(values[0], values[1]);
				}
				if(name.equals(field.getName()))
					fields.add(field);
			}
		}
		renderObjectOnTable(xmlDoc, idElement, title, obj, fields.toArray(new Field[]{}),alias);
	}
/*	public static void renderObjectOnTable(Document xmlDoc,String idElement,String title,Object obj){
		renderObjectOnTable(xmlDoc, idElement, title, obj, obj.getClass().getDeclaredFields());
	}*/
	public static void renderObjectOnTable(Document xmlDoc,String idElement,String title,Object obj,Field[] includeFields,HashMap<String, String> aliases){

		Element actionResponce = getActionResponce(xmlDoc);
		String textContent = "<elements>";
		if(actionResponce.getTextContent() != null && !actionResponce.getTextContent().equals("")){
			textContent = actionResponce.getTextContent();
			textContent = textContent.substring(0,textContent.indexOf("</elements>"));
		}
		
		StringBuilder strb = new StringBuilder(textContent);
		strb.append("<").append(idElement).append(">");
		strb.append("<br/><table id=\"").append(idElement).append("\" border=\"1\"  cellspacing=\"0\"   cellpadding=\"0\" style=\"display: table;table-layout: fixed;max-width: 100%;\">");
		strb.append("<tr width=\"50%\"><th colspan=\"2\">").append(title).append("</th></tr>");
		int i=0;
		String alias;
		for(Field field:includeFields){
			strb.append("<tr").append(i%2==0?" class=\"alt\">":">");
			alias = aliases.get(field.getName());
			if(alias != null)
				strb.append("<td><strong>").append(alias).append("</strong></td><td>");
			else
				strb.append("<td><strong>").append(field.getName()).append("</strong></td><td>");
			try {
				Object val = UtilReflection.getFieldValue(field, obj);
				if(val!=null)
					strb.append(val);
			}catch (Exception e) {
				System.err.println("Error getting value of field:"+field.getName());
			} 
			strb.append("</td>");
			strb.append("</tr>");
			i++;
		}
		strb.append("</table>");	
		strb.append("</").append(idElement).append("></elements>");
		
		
		actionResponce.setTextContent(strb.toString());
	}

	public static void renderListOnTable(Document xmlDoc,String idElement,List<?> objs,List<String> includeFields,String fieldKey){
		if(objs == null)
			return;
		List<Field> fields = new ArrayList<Field>(includeFields.size());
		HashMap<String, String> alias = new HashMap<String, String>();
		for(String name:includeFields){
			if(name.contains(":")){
				String[] values = name.split(":");
				name = values[0];
				alias.put(values[0], values[1]);
			}
			if(objs.size()>0){
				for(Field field:objs.get(0).getClass().getDeclaredFields()){
					if(name.equals(field.getName())){
						fields.add(field);
						break;
					}
				}
			}
		}

		renderListOnTable(xmlDoc, idElement, objs, fields.toArray(new Field[]{}),alias,fieldKey);
	}
	public static void renderListOnTable(Document xmlDoc,String idElement,List<?> objs,Field[] includeFields,HashMap<String, String> aliases,String fieldKey){
		if(objs == null)
			return;
		Element actionResponce = getActionResponce(xmlDoc);
		String textContent = "<elements>";
		if(actionResponce.getTextContent() != null && !actionResponce.getTextContent().equals("")){
			textContent = actionResponce.getTextContent();
			textContent = textContent.substring(0,textContent.indexOf("</elements>"));
		}
		
		StringBuilder strb = new StringBuilder(textContent);
		strb.append("<").append(idElement).append(">");
		strb.append("<table id=\"").append(idElement).append("\" border=\"1\"  cellspacing=\"0\"   cellpadding=\"0\" style=\"display: block;table-layout: fixed;max-width: 100%;\">");
		
		
		strb.append("<tr>");
		String alias;
		strb.append("</tr>");
		if(objs.size()>0){

			for(Field field:includeFields){
				alias = aliases.get(field.getName());
				if(alias != null)
					strb.append("<th>").append(alias).append("</th>");
				else
					strb.append("<th>").append(field.getName()).append("</th>");
			}
			
			for(Object obj:objs){
				strb.append("<tr");
				try {
					Object val = UtilReflection.getFieldValue(fieldKey, obj);
					if(val!=null)
						strb.append(" id=\"tr_").append(idElement).append("\" target=\"").append(val).append("\"");
				}catch (Exception e) {
					System.err.println("Error getting value of keyfield:"+fieldKey);
				} 
				strb.append(">");
				for(Field field:includeFields){
					strb.append("<td>");
					try {
						Object val = UtilReflection.getFieldValue(field, obj);
						if(val!=null)
							strb.append(val);
					}catch (Exception e) {
						System.err.println("Error getting value of field:"+field.getName());
					} 
					strb.append("</td>");
				}
				strb.append("</tr>");
			}
		}else{
			for(String field:aliases.keySet()){
				alias = aliases.get(field);
				if(alias != null)
					strb.append("<th>").append(alias).append("</th>");
				else
					strb.append("<th>").append(field).append("</th>");
			}
			strb.append("<tr> <td align=\"center\" colspan=\"").append(aliases.keySet().size()).append("\">No hay registros</td></tr>");
			
		}
		
		strb.append("</table>");	
		strb.append("</").append(idElement).append("></elements>");		
		
		actionResponce.setTextContent(strb.toString());
		
	}

	public static Element getActionResponce(Document xmlDoc) {
		Element actionResponce = (Element) xmlDoc.getElementsByTagName("action_responce").item(0);
		if(actionResponce == null){
			actionResponce = xmlDoc.createElement("action_responce");
			xmlDoc.getDocumentElement().appendChild(actionResponce);	
		};
		return actionResponce;
	}
	public static void appendToActionResponse(Document xmlDoc,String intoTag, String html) {
		Element actionResponce = XslUtil.getActionResponce(xmlDoc);
		String openTag  = String.format("<%s>", intoTag);
		String closeTag = String.format("</%s>", intoTag);
		String textContent = actionResponce.getTextContent();
		String parentTag;
		if(textContent != null && !actionResponce.getTextContent().equals("")){
			int i = textContent.indexOf(openTag);
			int j = textContent.indexOf(closeTag);
			if(i>=0 && j>i){
				parentTag   = textContent.substring(i,j+closeTag.length());
				textContent = textContent.replace(parentTag, "%s");

				parentTag   = parentTag.substring(0,parentTag.length()-closeTag.length());
				parentTag   = String.format("%s%s%s", parentTag,html,closeTag);

				actionResponce.setTextContent(String.format(textContent, parentTag));
			}else{
				actionResponce.setTextContent(String.format("%s%s%s%s",textContent, openTag,html,closeTag));
			}
		}else{
			actionResponce.setTextContent(String.format("%s%s%s", openTag,html,closeTag));
		}
		
	}
	/**
	 * Crea un el tag JSON_DATA en xmlDoc, el cúal contiene el paremetro obj serializado en json
	 * @param xmlDoc
	 * @param obj
	 */
	public static void createJsonResult(Document xmlDoc,Object obj){
		Element root = xmlDoc.getDocumentElement();
		Element json = xmlDoc.createElement("JSON_DATA");
		root.appendChild(json);						 
		json.setTextContent(JSONUtil.encodeJsonString(obj));
	}
	
	public static Element getTagPermisos(Document xmlDoc){
		try {
		    Element root = xmlDoc.getDocumentElement();
		    Element node = (Element)root.getFirstChild();
		      
	        if (!node.getNodeName().equals("SPHINX"))  return null;
	        node = (Element) node.getFirstChild();
	        
	        while (node != null && !node.getNodeName().equals("PERMISOS"))
	          node = (Element)node.getNextSibling();

	        return node;
	    } catch(Exception ex) {
	    	ex.printStackTrace();
	    }
		return null;
	}

}
