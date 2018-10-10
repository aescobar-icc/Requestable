package beauty.requestable.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import beauty.requestable.core.annotations.RequestableClass;
import beauty.requestable.core.annotations.RequestableOperation;
import beauty.requestable.core.annotations.inject.InjectFactory;
import beauty.requestable.core.annotations.inject.InjectCreator;
import beauty.requestable.core.enums.RequestableType;
import beauty.requestable.core.exceptions.RequestableException;
import beauty.requestable.core.interfaces.MultiUrl;
import beauty.requestable.core.interfaces.ParameterParser;
import beauty.requestable.core.url.UrlPattern;
import beauty.requestable.util.file.FileUtil;
import beauty.requestable.util.reflection.UtilReflection;
import beauty.requestable.util.serialize.JSONException;
import beauty.requestable.util.serialize.JSONUtil;
import beauty.requestable.util.stream.StreamUtil;
import beauty.requestable.util.string.StringBuilderUtil;
import beauty.requestable.util.xml.UtilDOM;



public class ServletUtil {
	private static final Object sync = new Object();
	private static final String IDENTIFIER_FORMAT = "[%s] %s";
	
	private static ServletContext servletContext;

	private static Hashtable<String, Class<?>> requestableClass = null;
	private static Hashtable<Class<?>,Class<?>> beanClass = null;
	private static List<Class<? extends MultiUrl>> multiUrlWebPages = new ArrayList<Class<? extends MultiUrl>>();
	//private static List<Class<?>> filterClass = null;
	private static String basePath = null;
	private static Document requestableXML = null;

	public static Class<?> getRequestableClass(String identifier,RequestableType type){
		synchronized (sync) {
			if(requestableClass == null)
				throw new RuntimeException("[ServletUtil] requestableClass has not been initialized properly. You must call ServletUtil.findRequestableClass(context) first.");
			
			return requestableClass.get(String.format(IDENTIFIER_FORMAT, type,identifier));
		}
	}
	public static Class<?> getBeanFactory(Class<?> type){
		synchronized (sync) {
			if(beanClass == null)
				throw new RuntimeException("[ServletUtil] beanClass has not been initialized properly. You must call ServletUtil.findRequestableClass(context) first.");
			
			return beanClass.get(type);
		}
	}
	public static boolean isReadyRequestable(){
		synchronized (sync) {
			return requestableClass != null;
		}
	}

	public static Class<?> getFromMultiUrl(ServletRequest request, String requestURI) {
		int priority = 0,pp;
		UrlPattern pattern = null;
		Class<? extends MultiUrl> match = null;
		for(Class<? extends MultiUrl> c: multiUrlWebPages) {
			try {
				MultiUrl ins = c.newInstance();
				List<UrlPattern> patterns = ins.getUrlPatterns();
				if(patterns != null) {
					for(UrlPattern p:patterns) {
						if( (pp = p.eval(requestURI)) > priority) {
							priority = pp;
							match = c;
							pattern = p;
						}
					}
				}
			} catch (Throwable e) {
				System.err.println("[ServeletUtil] error checking url pattern for "+c.getName());
				e.printStackTrace();
			}
		}
		if(pattern != null)
			request.setAttribute(RequestableService.ATTR_PAGE_URL_PATTERN,pattern);
		return match;
	}
	public static Method getRequestableMethod(Class<?> clss,String methodIdentifier) {
		List<Method> methods = UtilReflection.getAllAnnotatedMethods(clss, RequestableOperation.class);
		RequestableOperation annotation;
		for(Method m:methods){
			annotation = m.getAnnotation(RequestableOperation.class);
			if(annotation.identifier().equals(methodIdentifier))
				return m;
		}
		return null;
	}

	/*public static Object invokeRequestableMethod(HttpServletRequest request,HttpServletResponse response, Object obj,Method method) throws Throwable  {
		Object[] params = getRequestableMethodParams(request, response, method);
		return invokeRequestableMethod(obj, method, params);
	}*/
	public static Object invokeRequestableMethod(Object obj, Method method,Object[] params) throws Throwable {
		String methodName = "";
		try{
			methodName = String.format(" %s.%s",method.getDeclaringClass().getSimpleName(), method.getName());
			UtilReflection.methodParamsValidate(method, params);
			return method.invoke(obj, params);	
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(methodName,e);
		}catch (InvocationTargetException e) {
			Throwable cause = e;
			while(cause.getCause() != null){
				if(cause.getClass().isAssignableFrom(RequestableException.class))
					break;
				cause = cause.getCause();
			}
			throw cause;
				
		}
		
	}
	public static <T extends Annotation> Object[] getRequestableMethodParams(HttpServletRequest request, Method method,String[] parameterNames) throws RequestableException, JSONException{
		@SuppressWarnings("unchecked")
		HashMap<String,Object> values = (HashMap<String,Object>)request.getAttribute("parameters");
		if(values != null) {
			Object[] params = new Object[parameterNames.length];
			for(int i=0;i<parameterNames.length;i++) {
				params[i] = values.get(parameterNames[i]);
			}
			return params;
		}else {

			values = new HashMap<String, Object>();
			for(String name:parameterNames) {
				values.put(name, request.getParameter(name));
			}
			return getRequestableMethodParams(method, parameterNames, values);
		}
		
	}
	/*public static <T extends Annotation> Object[] getRequestableMethodParams(HttpServletRequest request,HttpServletResponse response, Method method,Class<T> annotationClass) throws Throwable {
		T methodAnnotation = method.getAnnotation(annotationClass);
		Type[]   paramsTypes	= method.getGenericParameterTypes();
		Object[] params			= new Object[paramsTypes.length];
		StringBuilder invokeDetail = new StringBuilder(String.format("[InvokeRequestableMethod] %s.%s",method.getDeclaringClass().getName(), method.getName()));

		String[] parameterNames = (String[]) UtilReflection.getAnnotationValue(methodAnnotation,"parameters");*/
	public static Object[] getRequestableMethodParams(Method method,String[] parameterNames,HashMap<String,Object> values) throws RequestableException, JSONException {
		Type[]   paramsTypes	= method.getGenericParameterTypes();
		Object[] params			= new Object[paramsTypes.length];
		try{
			//invokeDetail.append("(");
			if( paramsTypes.length > 0 ){
				String   parameterName = null;
				Class<?> parameterType = null; 
				for(int i=0;i< paramsTypes.length;i++){
					try{
						parameterName = parameterNames[i];
						if(paramsTypes[i] instanceof Class){
							parameterType = (Class<?>)paramsTypes[i];
		
							if(values.get(parameterName)!= null){
								if(ParameterParser.class.isAssignableFrom(parameterType))
									try {
										params[i] = ((ParameterParser<?>)parameterType.newInstance()).parse((String)values.get(parameterName));
									} catch (Throwable e) {
										throw new RequestableException(String.format("Error invocando parser parámetro %s: Detalle: %s",parameterName,e.getMessage()));
									}
								else if(List.class.isAssignableFrom(parameterType)){
									params[i] = JSONUtil.decodeList((String)values.get(parameterName), parameterType);
								}else if(parameterType.isArray()){
									params[i] = JSONUtil.decodeArray((String)values.get(parameterName), parameterType);
								}else 
									params[i] = JSONUtil.decodeObject((String)values.get(parameterName), parameterType);
							}
						}else if(paramsTypes[i] instanceof ParameterizedType){
							 ParameterizedType parameterizedType = (ParameterizedType) paramsTypes[i];
							 parameterType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
							 params[i] = JSONUtil.decodeList((String)values.get(parameterName),  parameterType);
						}
						//invokeDetail.append(String.format("%s:%s = %s, ",parameterName,parameterType.getCanonicalName(),params[i]));
					}catch(JSONException e){
						throw new JSONException(String.format("Error decodificando parámetro %s: Detalle: %s",parameterName,e.getMessage()));
					}
				}
				//StringBuilderUtil.replaceLast(invokeDetail, ", ", "");
			}		
			//invokeDetail.append(")");
			//System.out.println(invokeDetail.toString());	
			return params;	
		}catch (ArrayIndexOutOfBoundsException e) {
			//String methodName = (String) UtilReflection.getAnnotationValue(methodAnnotation,"identifier");
			//if(methodName == null)
			String methodName = method.getName();
			//throw new RequestableException(String.format("El número de parámetros definidos en el atributo 'parameters' de la %s:%s, NO coinciden con el número del método anotado",annotationClass.getSimpleName(),methodName), e);
			throw new RequestableException(String.format("El número de parámetros definidos en método:%s, no conside con parametros en anotacion",methodName));
		}
		
	}
	@SuppressWarnings("unchecked")
	public static void findAllRequestableClass(){
		synchronized (sync) {
			if(requestableClass == null){
				log("Finding All Requestable Class");
				requestableClass = new Hashtable<String, Class<?>>();
				String[] classPath = null;
				try{
					classPath = readLocations("locations","location");
				}catch(RuntimeException e){
					System.err.println(String.format("[ServletUtil] %s\n",e.getMessage()));
					System.out.println("[ServletUtil] Se usarán rutas por defecto.");
			    	classPath = new String[2];
		    		String basePath = resolveWEBINF();
		    		System.out.println("[REQUESTABLE] base path"+basePath);
			    	classPath[0] = basePath+"/lib";
			    	classPath[1] = basePath+"/classes";
			    	
					System.out.println("[REQUESTABLE] default find in 'WEB-INF/lib': "+classPath[0]);
					System.out.println("[REQUESTABLE] default find in 'WEB-INF/classes': "+classPath[1]);

				}

				Hashtable<String, List<String>> duplicateClassIdentifier	= new Hashtable<String, List<String>>();
				Hashtable<String, List<String>> duplicateMethodIdentifiers 	= new Hashtable<String, List<String>>();
				List<Class<?>>	requestableClassFound	= UtilReflection.listAnnotadedClass(RequestableClass.class, classPath);
				//filterClass								= UtilReflection.listAnnotadedClass(FilterClass.class, classPath);
				String classIdentifier = null;
				String methodIdentifier;
				RequestableClass annotation;
				for(Class<?> c:requestableClassFound){
					annotation = c.getAnnotation(RequestableClass.class);
					try{
						switch (annotation.type()) {
							case WEB_PAGE:
								classIdentifier = String.format(IDENTIFIER_FORMAT, annotation.type(),annotation.uri());
								if(MultiUrl.class.isAssignableFrom(c)) {
									multiUrlWebPages.add((Class<? extends MultiUrl>) c);
								}
								break;
							case COMPONENT:
							case DOWNLOAD_FILE_WRITER:
							case SERVICE:
								classIdentifier = String.format(IDENTIFIER_FORMAT, annotation.type(),annotation.identifier());
								break;
						}
					}catch(IncompleteAnnotationException e){
						int index = e.toString().indexOf("missing");
						throw new RuntimeException(String.format("%s in class annotated %s with RequestableClass",e.toString().substring(index),c.getName()));
					}
					requestableClass.put(classIdentifier, c);
					
					registerDuplicate(duplicateClassIdentifier, classIdentifier, c.getName());
					
					for(Method m:UtilReflection.getAllAnnotatedMethods(c, RequestableOperation.class)){
						methodIdentifier = m.getAnnotation(RequestableOperation.class).identifier();
						registerDuplicate(duplicateMethodIdentifiers, classIdentifier+"."+methodIdentifier, c.getName() +":"+m.getName());
					}
				}
				
				//check for duplicate
				boolean hasDuplicate = checkDuplicate(duplicateClassIdentifier);
				if(hasDuplicate){
					requestableClass = null;
					throw new RuntimeException(String.format("Class Identifier: %s is duplicated",enumToString(duplicateClassIdentifier.keys())));
				}
				hasDuplicate = checkDuplicate(duplicateMethodIdentifiers);
				if(hasDuplicate){
					requestableClass = null;
					throw new RuntimeException(String.format("Class Methods Identifier: %s is duplicated",enumToString(duplicateMethodIdentifiers.keys())));
				}
				if("on".equals(System.getProperty("ServletUtil.showDebug"))){
					Enumeration<String> classes = requestableClass.keys();
					while(classes.hasMoreElements()){
						System.out.println(String.format("[ServletUtil] Requestable Class Found: %s",classes.nextElement()));
					}
				}
			}
			if(beanClass == null) {
				beanClass = new Hashtable<Class<?>,Class<?>>();
				String[] classPath = null;
				try{
					classPath = readLocations("beans","bean");
				}catch(RuntimeException e){
				}
				if(classPath != null) {
					List<Class<?>>	factoryClassFound	= UtilReflection.listAnnotadedClass(InjectFactory.class, classPath);

					for(Class<?> c:factoryClassFound){
						List<Method> methods = UtilReflection.getAllAnnotatedMethods(c, InjectCreator.class);
						for(Method m:methods) {
							if(beanClass.contains(m.getReturnType())) {
								throw new RuntimeException(String.format("InjectFactory type creator: %s is duplicated",m.getReturnType()));
							}
							beanClass.put(m.getReturnType(),c);
						}
					}
				}
				
			}
		}		
	}
	/**
	 * Returns the real path of WEB-INF directory
	 * @return
	 */
	public static String resolveWEBINF() {
		if(basePath == null){
			/*String path  = UtilReflection.getRealPath(ServletUtil.class);
			int i = path.indexOf("file:")+5;
			int j = path.indexOf("WEB-INF")+7;
			basePath = path.substring(i, j)+"/";*/
			basePath = servletContext.getRealPath("WEB-INF/");
		}
		return basePath;
	}
	public static Document getRequestableXML() throws ParserConfigurationException, SAXException, IOException{
		if(requestableXML == null){
			String url = resolvePrivateFileLocation("conf/requestable.xml");
			requestableXML = UtilDOM.createDocument(url);
		}
		return requestableXML;
	}
	private static String[] readLocations(String group,String kind) {
		String[] urls = null;
		try {
			Document xml = getRequestableXML();
			List<Node> locations = UtilDOM.searchNode(xml, group);
			if(locations.size() == 0 )
				throw new RuntimeException("archivo no define tag locations");			

			locations = UtilDOM.searchNode(locations.get(0), kind);
			if(locations.size() == 0 )
				throw new RuntimeException("no se ha definido ninguna location válida");
			
			urls	= new String[locations.size()];
			int i=0;
			for(Node loc:locations){
				String url = UtilDOM.getAttributeValue(loc, "package");
				url = url.replaceAll("\\.", "/");
				if(url.startsWith("jar:")) {
					url.replace("jar:", "/lib/");
				}else {
					url = "/classes/"+url;
				}
				System.out.println(url);
				if(url != null){
					url = basePath+"/"+url;
					//System.out.println(url);
					urls[i] = url;
					i++;
				}
			}
			
		} catch (Throwable e) {
			throw new RuntimeException("Error leyendo requestable.xml, "+ e.getMessage());
		}
		return urls;
	}
	private static Object enumToString(Enumeration<String> keys) {
		StringBuilder strb = new StringBuilder();
		while(keys.hasMoreElements())
			strb.append(keys.nextElement()).append(", ");
		return strb.toString();
	}
	private static void registerDuplicate(Hashtable<String, List<String>> duplicates,String identifier,String name){
		List<String> list;
		if(duplicates.containsKey(identifier)){
			list = duplicates.get(identifier); 
		}else{
			list = new ArrayList<String>();
			duplicates.put(identifier, list);
		}
		list.add(name);
	}
	private static boolean checkDuplicate(Hashtable<String, List<String>> duplicates){
		List<String> list;
		Enumeration<String> identifiers = duplicates.keys();
		String identifier;
		boolean hasDuplicate = false;
		while(identifiers.hasMoreElements()){
			identifier = identifiers.nextElement();
			list = duplicates.get(identifier);
			if(list.size() > 1){
				hasDuplicate = true;
				System.err.println(String.format("[ServletUtil] IDENTIFIER: %s duplicate on:",identifier));
				for(String c:list)
					System.err.println(String.format("\t\t %s",c));
			}else
				duplicates.remove(identifier);
		}
		return hasDuplicate;
	}
	
	public static String getServerAddress(HttpServletRequest request) throws UnknownHostException{

		StringBuilder strb = new StringBuilder();
		if(request.isSecure())
			strb.append("https://");
		else
			strb.append("http://");

		//append id
		//strb.append(InetAddress.getLocalHost().getHostAddress()).append(":");
		//append port
		//strb.append(request.getLocalPort());
		String host = request.getHeader("host");
		if(host == null || host.equals("") || "unknown".equalsIgnoreCase(host)){
			host = InetAddress.getLocalHost().getHostAddress()+":"+request.getLocalPort();
		}
		//append host
		strb.append(host);
		//append App Context
		strb.append(request.getServletContext().getContextPath());
		return strb.toString();
	}
	public static String getClientAddress(HttpServletRequest request) {
		
		String ip = "127.0.0.1";
		if (request == null)
			return ip;
		
		ip = request.getHeader("X-Forwarded-For");
		
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("x-forwarded-for");  
	    }  
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("Proxy-Client-IP");  
	    }  
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("WL-Proxy-Client-IP");  
	    }  
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("HTTP_CLIENT_IP");  
	    }  
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
	    }  
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getRemoteAddr();  
	    }  
	    return ip;  		
	}

	public static void printParameters(HttpServletRequest request){
		System.out.println("-----------------------------------PARAMETERS-----------------------------------");
		Enumeration<String> par = request.getParameterNames();
		while(par.hasMoreElements()){
			String param = par.nextElement();
			System.out.println(String.format("%s=%s", param,request.getParameter(param)));
		}
	}
	public static HashMap<String, String> getParameters(HttpServletRequest request){
		HashMap<String, String> params = new HashMap<String, String>();
		Enumeration<String> par = request.getParameterNames();
		while(par.hasMoreElements()){
			String param = par.nextElement();
			params.put(param,request.getParameter(param));
		}
		return params;
	}
	public static void printSession(HttpSession s){
		System.out.println("-----------------------------------SESSION-----------------------------------");
		
		System.out.println(String.format("session=%s",s.getId()));
		Enumeration<String> names = s.getAttributeNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			System.out.println(String.format("%s=%s", name,s.getAttribute(name)));
		}
		
	}
	public static void printHeaders(HttpServletRequest request,OutputStream output){
		PrintStream out = new PrintStream(output);
		out.println("-----------------------------------HEADERS-----------------------------------");
		
		Enumeration<String> names = request.getHeaderNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			out.println(String.format("%s: %s", name,request.getHeader(name)));
		}
		
	}
	public static void printHeaders(HttpServletRequest request){
		printHeaders(request,System.out);
	}
	public static void readInputStream(HttpServletRequest request) throws IOException{
		System.out.println("-----------------------------------INPUT STREAM-----------------------------------");

		ServletInputStream inputStream = request.getInputStream();
		ByteArrayOutputStream bytes = StreamUtil.readInputStream(inputStream);
		System.out.println(bytes.toString());
	}
	
	/**
	 * Read File From WEB-INF directory
	 * @throws IOException 
	 */
	public static byte[] readPrivateFile(String fileUri) throws IOException{
		String path = resolvePrivateFileLocation(fileUri);
		System.out.println("[ServletUtil] try read file: "+path);
		return FileUtil.readFile(path);
	}
	/**
	 * Check and return location if private file exists
	 * @param fileUri
	 * @return
	 * @throws IOException
	 */
	public static String resolvePrivateFileLocation(String fileUri) throws IOException{
		String basePath = resolveWEBINF();
		if((new File(basePath+"classes/"+fileUri)).exists()){ // for maven project
			return basePath+"classes/"+fileUri;
		}
		if((new File(basePath+fileUri)).exists()){ // others
			return basePath+fileUri;
		}
		
		throw new IOException(String.format("private file: %s does not exists",fileUri));
	}
	
	private static void log(String msg){
		if("on".equals(System.getProperty("ServletUtil.showDebug")))
			System.out.println("[ServletUtil] "+msg);
	}
	public static void setServletContext(ServletContext servletContext) {
		ServletUtil.servletContext = servletContext;
	}
}
