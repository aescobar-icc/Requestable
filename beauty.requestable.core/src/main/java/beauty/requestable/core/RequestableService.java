package beauty.requestable.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beauty.requestable.core.annotations.RequestableClass;
import beauty.requestable.core.annotations.RequestableOperation;
import beauty.requestable.core.annotations.filter.RequestableFilter;
import beauty.requestable.core.annotations.filter.RequestableFilterOperation;
import beauty.requestable.core.annotations.inject.InjectCreator;
import beauty.requestable.core.annotations.inject.Injected;
import beauty.requestable.core.constants.OperationDefaults;
import beauty.requestable.core.descriptors.RequestableComponentDescriptor;
import beauty.requestable.core.enums.FilterApply;
import beauty.requestable.core.enums.RequestableType;
import beauty.requestable.core.exceptions.RequestableException;
import beauty.requestable.core.exceptions.RequestableFlowExeption;
import beauty.requestable.core.exceptions.RequestableSecurityException;
import beauty.requestable.core.flowcontrol.RequestableFlowControl;
import beauty.requestable.core.interfaces.FlowControlHandler;
import beauty.requestable.core.interfaces.RequiereDaoSchema;
import beauty.requestable.core.interfaces.RequiereEnviorement;
import beauty.requestable.core.interfaces.RequiereSession;
import beauty.requestable.core.url.UrlPattern;
import beauty.requestable.util.reflection.UtilReflection;
import beauty.requestable.util.serialize.JSONException;
import beauty.requestable.util.serialize.JSONUtil;
import beauty.requestable.util.sql.ConnectionManagerException;
import beauty.requestable.util.sql.schema.IDaoSchema;


/**
 * Servlet implementation class RequestableService
 */
@WebServlet("/beauty.requestable.service")
public class RequestableService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String ATTR_CLASS = "requestable.class";
	public static final String ATTR_PAGE_URL_PATTERN = "requestable.webpage.patternmatch";
	

	private static final String COMPONENT_REGEX = "<div\\s+.*data-component=\"([_a-zA-Z0-9\\$\\{\\}]+)\"([^\\/>]*\\/>|[^>]*(([^<]\\r?\\n?)*<\\/div>)?)";
	private static final String PAGE_HEAD_REGEX  = "<head>((.*\\r?\\n)*)<\\/head>";
	private static final String COMPONENT_ID_REGEX = "^\\$\\{([^}]*)\\}$";
	
	public static final List<String> onProgress = new ArrayList<String>();
	public static final Object sync = new Object();
	
	public static Class<? extends IDaoSchema> daoSchemaClass = null;
	
	private HashMap<String, RequestableComponentDescriptor> htmlComponents = new HashMap<String, RequestableComponentDescriptor>();



	/**
	 * Default constructor.
	 */
	public RequestableService() {
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestableResponse resp = new RequestableResponse();
		String tokenCurrentOperation = null;
		String identifier = null;
		String operationName = null;
		//String action = null;
		RequestableType type = null;
		boolean endOK = false;
		boolean flowChange = false;
		RequestableClass classAnnotation = null;
		Class<?> serviceClass = null;
		Object service = null;
		try {
			// ServletUtil.printHeaders(request);
			tokenCurrentOperation = getTokenCurrentOperation(request);

			ServletUtil.findAllRequestableClass();

			// Obtiene identificadores del Servicio
			serviceClass = (Class<?>) request.getAttribute(RequestableService.ATTR_CLASS);
			if(serviceClass == null)
				identifier = (String)request.getAttribute("identifier");
			else{
				request.setAttribute(RequestableService.ATTR_CLASS,null);// set to null to avoid infinite loop 
				classAnnotation = serviceClass.getAnnotation(RequestableClass.class);
				switch (classAnnotation.type()) {
					case COMPONENT:
						break;
					case DOWNLOAD_FILE_WRITER:
						break;
					case SERVICE:
						break;
					case WEB_PAGE:
						identifier = classAnnotation.uri();
						break;
					default:
						break;
				}
			}
			if(identifier == null)
				identifier = request.getParameter("identifier");
			operationName = (String)request.getAttribute("operation");
			if(operationName == null)
				operationName = request.getParameter("operation");
			type = (RequestableType)request.getAttribute("type");
			if(type == null)
				type = RequestableType.getByName(request.getParameter("type"));

			if (identifier == null || identifier.equals(""))
				throw new RequestableException("ERROR: identifier no tiene un valor válido.");
			if (operationName == null || operationName.equals(""))
				throw new RequestableException("ERROR: operation no tiene un valor válido.");

			// Obtiene la clase del servicio
			if (serviceClass == null)
				serviceClass = ServletUtil.getRequestableClass(identifier, type);
			if (serviceClass == null)
				throw new RequestableException(String.format("ERROR: El %s='%s' NO existe.", type, identifier));
			// Obtiene la anotacion del servicio
			if(classAnnotation == null)
				classAnnotation = serviceClass.getAnnotation(RequestableClass.class);

			// verifica los filtros de clase
			doFilter(request,response,serviceClass,classAnnotation,FilterApply.ALL_METHOD);

			// Obtiene la operación
			Method operation = ServletUtil.getRequestableMethod(serviceClass, operationName);
			
			Object serviceResponse = null;
			if (operation != null ) {
				//ServletUtil.printParameters(request);
				// valida.checkAllowAcces(operation);
				addOnProgress(tokenCurrentOperation, operation);

				RequestableOperation operaAnnotation = operation.getAnnotation(RequestableOperation.class);

				// obtiene parametros de la operación
				String[] paramNames = operaAnnotation.parameters();
				Object[] paramValues = ServletUtil.getRequestableMethodParams(request,operation, paramNames);//(request, response, operation,RequestableOperation.class);
				

				// Crea instancia del servicio
				service = serviceClass.newInstance();
				injectDependencies(request, service);

				// invoca operación del servicio
				serviceResponse = ServletUtil.invokeRequestableMethod(service, operation, paramValues);

				endOK = true;
			}else {
				if(OperationDefaults.WEB_PAGE_INIT.equals(operationName) && classAnnotation.type() == RequestableType.WEB_PAGE) {
					// method init webpage no estricto
					serviceResponse = request.getAttribute("parameters");
					endOK = true;
				}else
					throw new RequestableException(String.format("ERROR: El %s='%s' NO define Operación=%s.", type,identifier, operationName));
			}

			resp.setOk(true);
			resp.setData(serviceResponse);
		}catch (RequestableFlowExeption e) {
			flowChange = true;
		} catch (RequestableSecurityException|RequestableException e) {
			resp.addMessage(e.getMessage());
		} catch (IllegalArgumentException e) {
			System.err.println("[AjaxService] EXCEPCION CONTROLADA: ");
			e.printStackTrace();
			resp.addMessage(String.format("ERROR: Los parámetros entregados para la operación:%s no son válidos.", e.getMessage()));
		} catch (JSONException e) {
			Throwable cause = e;
			while (cause.getCause() != null)
				cause = cause.getCause();
			resp.addMessage(String.format("ERROR: No se pudo transformar uno de los parámetros al tipo esperado.\nDetalle: %s.", cause.toString()));
		} catch (Throwable e) {
			System.err.println("[AjaxService] EXCEPCION CONTROLADA: ");
			e.printStackTrace();
			resp.addMessage(String.format("Error executing %s.%s.\nDetalle:%s", identifier, operationName, e.toString()));
		} finally {
			removeOnProgress(tokenCurrentOperation);
			if(service != null)
				destroyDependencies(request, service);
		}

		if(!flowChange) {
			PrintWriter writer = response.getWriter();
			response.setCharacterEncoding("UTF-8");
			
			switch (type) {
				case WEB_PAGE:
					if(endOK) {
						String uriRender = classAnnotation != null? classAnnotation.render():"";
						responseWebPage(request,response,uriRender,identifier,resp);
					}else
						writer.print(resp.getMessages());
					break;
				case COMPONENT:
				case DOWNLOAD_FILE_WRITER:
				case SERVICE:
				default:
					response.setContentType("application/json; charset=UTF-8");
					String json = resp.parseJson();
					//System.out.println(json);
					writer.print(json);
				break;
			}
		}
		
	}

	private void doForward(HttpServletRequest request, HttpServletResponse response, RequestableFlowControl control) throws ServletException, IOException {
		System.out.println("/beauty.requestable.service");
		request.setAttribute("identifier",control.getRedirectUri());
		request.setAttribute("operation",OperationDefaults.WEB_PAGE_INIT);
		request.setAttribute("type",RequestableType.WEB_PAGE);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/beauty.requestable.service");
		dispatcher.forward(request,response);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, Class<?> serviceClass,RequestableClass reqClass, FilterApply apply) throws RequestableFlowExeption, RequestableException{

		RequestableFilter[] filterAnnotations = serviceClass.getAnnotationsByType(RequestableFilter.class);
		for (RequestableFilter filterAnn : filterAnnotations) {
			if (filterAnn.filter() == null)
				throw new RequestableException(String.format("ERROR: El %s='%s' NO define filter class.",reqClass.type(), reqClass.identifier()));
			if (filterAnn.apply() == apply) {
				try {
					Class<?> filterClass = filterAnn.filter();
					Object instance = filterAnn.filter().newInstance();
					injectDependencies(request, instance);
	
					List<Method> filterMethods = UtilReflection.getAllAnnotatedMethods(filterClass,RequestableFilterOperation.class);
					for (Method filterMethod : filterMethods) {
						RequestableFilterOperation filterMethodAnnotation = filterMethod.getAnnotation(RequestableFilterOperation.class);
						Object[] filterParamValues = ServletUtil.getRequestableMethodParams(request, filterMethod, filterMethodAnnotation.parameters());
						try {
							ServletUtil.invokeRequestableMethod(instance, filterMethod, filterParamValues);
						} catch (RequestableSecurityException e) {
							if (FlowControlHandler.class.isAssignableFrom(filterClass)) {
								RequestableFlowControl flowControl = new RequestableFlowControl(reqClass, e);
								((FlowControlHandler) instance).onSecurityException(flowControl);
								switch (flowControl.getFlowType()) {
									case FORWARD:
										request.setAttribute("parameters", flowControl.getParams());
										doForward(request, response, flowControl );
										break;
									case REDIRECT:
										break;
									default:
										break;
								}
								throw new RequestableFlowExeption(flowControl);
							}
						}
					}
				}catch (RequestableException | RequestableFlowExeption e) {
					throw e;
				}catch (Throwable e) {
					throw new RequestableException("Error procesando filter",e);
				}
			}
		}

	}

	private void responseWebPage(HttpServletRequest request,HttpServletResponse response, String uriRender, String identifier,	RequestableResponse resp) throws IOException {
		PrintWriter writer = response.getWriter();
		response.setContentType("text/html; charset=UTF-8");
		if(!uriRender.equals("")) {
			try {
				byte[] file = ServletUtil.readPrivateFile(uriRender);
				String htmlPage = new String(file);
				List<RequestableComponentDescriptor> components = new ArrayList<RequestableComponentDescriptor>();

				StringBuilder htmlBuilder = new StringBuilder();
				StringBuilder jsBuilder = new StringBuilder();
				StringBuilder cssBuilder = new StringBuilder();

				
				
				
				//find components
				int k =0;
				int uid = 0;
				Matcher m = Pattern.compile(COMPONENT_REGEX).matcher(htmlPage);
				while (m.find()) {
				  System.out.println("start:"+m.start()+" end:"+m.end());
				  String componentId = m.group(1);
				  Matcher mId = Pattern.compile(COMPONENT_ID_REGEX).matcher(componentId);
				  if(mId.find()) {
					  componentId = mId.group(1);
					  UrlPattern pattern = (UrlPattern) request.getAttribute(RequestableService.ATTR_PAGE_URL_PATTERN);
					  if(pattern != null) {
						  componentId = (String) pattern.getInitData().get(componentId);
					  }else {
						  try {
							  HashMap<String, Object> initData = (HashMap<String, Object>) resp.getData();
							  componentId = (String) initData.get(componentId);
						  }catch (Throwable e) {}
					  }
				  }
				  System.out.println("componentId:"+componentId);
				  System.out.println(htmlPage.substring(m.start(), m.end()));
				  
				  RequestableComponentDescriptor descriptor = getComponentDescriptor(componentId);
				  
				  if(descriptor != null) {					  
					  Object instance = descriptor.getComponentClass().newInstance();
					  String cuid = componentId+uid;
					  
					  components.add(descriptor);
					  for(String js:descriptor.getJs()) {
						  jsBuilder.append(js);
					  }
					  for(String css:descriptor.getCss()) {
						  cssBuilder.append(css);
					  }
					  if(k<m.start())
						  htmlBuilder.append(htmlPage.substring(k,m.start()));
					  if(descriptor.getHtml() != null) {
						  htmlBuilder.append(descriptor.getHtml().replaceFirst("<div", "<div data-component-id=\""+cuid+"\""));
					  }
					  
					  Method initOperation = ServletUtil.getRequestableMethod(descriptor.getComponentClass(), OperationDefaults.WEB_PAGE_INIT);
					  
					  if(initOperation != null) {
						  Object serviceResponse = ServletUtil.invokeRequestableMethod(instance, initOperation, new Object[] {});
						  if(serviceResponse != null) {
							  RequestableInitData.addData(request, identifier,cuid, serviceResponse);
						  }
					  }
					  
					  uid++;
					  k = m.end()+1;
				  }else {
					  
				  }
				  
				}
				
				if(k > 0) {
					if(k < htmlPage.length()) {
						htmlBuilder.append(htmlPage.substring(k));
						htmlPage = htmlBuilder.toString();
					}
					
					//find head
					m = Pattern.compile(PAGE_HEAD_REGEX).matcher(htmlPage);
					if (m.find()) {
						htmlPage = htmlPage.substring(0, m.start())+"<head>"+m.group(1)+cssBuilder.toString()+jsBuilder.toString()+"</head>"+htmlPage.substring(m.end());
					}
				}	
				
				
				writer.print(htmlPage);
				//System.identityHashCode() 
				RequestableInitData.addData(request, identifier,"page", resp.getData());
			}catch (Throwable e) {
				writer.print("render not found:"+uriRender);
			}
		}else {
			if(resp.isOk()) {
				writer.print(resp.getData());
			}else {
				for(String msg:resp.getMessages()){
					writer.println(msg);
				}
			}
		}
	}

	private RequestableComponentDescriptor getComponentDescriptor(String componentId) {
		if (!htmlComponents.containsKey(componentId)) {
			Class<?> componentClass = ServletUtil.getRequestableClass(componentId, RequestableType.COMPONENT);
			if (componentClass != null) {
				String uriRender = componentClass.getAnnotation(RequestableClass.class).render();
				RequestableComponentDescriptor descriptor = new RequestableComponentDescriptor();
				descriptor.setComponentClass(componentClass);
				try {
					byte[] fileJson = ServletUtil.readPrivateFile(uriRender.replaceAll("\\.html$", ".json"));
					JsonObject json = JSONUtil.getJsonObject(new String(fileJson));
					JsonValue css = json.get("css");
					if (css != null) {
						for (JsonValue cs : css.asJsonArray()) {
							descriptor.getCss().add("<link type=\"text/css\" rel=\"stylesheet\" href=" + cs + ">\n");
						}
					}
					JsonValue jss = json.get("js");
					if (jss != null) {
						for (JsonValue js : jss.asJsonArray()) {
							descriptor.getJs().add("<script type=\"text/javascript\" src=" + js + "></script>\n");
						}
					}
				}catch(IOException e) {
				}catch (Throwable e) {
					System.err.format("[RequestableService] error loading json uriRender:%s Detail:%s\n",uriRender,e.getMessage());
				}

				byte[] fileComp;
				try {
					fileComp = ServletUtil.readPrivateFile(uriRender);
					descriptor.setHtml(new String(fileComp));
				} catch (IOException e) {
					System.out.println("[RequestableService] component: "+componentId+" no define render");
				}
				
				htmlComponents.put(componentId, descriptor);
			}else {
				System.out.println("[RequestableService] NO existe component: "+componentId);
			}
				
		}
		return htmlComponents.get(componentId);
	}

	private String resolveComponentPath(Class<?> serviceClass) {
		String base = "components/";
		String uri = UtilReflection.getRealPath(serviceClass);
		uri = uri.substring(uri.indexOf(base) + base.length(), uri.indexOf(".class"));
		return uri;
	}

	private HashMap<String, Object> load(String uri) throws RequestableException {
		try {
			System.out.println("Loading files component:" + uri);
			String file = new String(ServletUtil.readPrivateFile(String.format("components/%s.html", uri)));
			HashMap<String, Object> info = new HashMap<String, Object>();
			info.put("html", file);
			info.put("js", uri + ".js");

			return info;
		} catch (IOException e) {
			throw new RequestableException(String.format("UI componentes ../%s.html is not found", uri,uri));
		}
	}
	private void destroyDependencies(HttpServletRequest request, Object service) {
		Class<?> type = service.getClass();
		if (RequiereDaoSchema.class.isAssignableFrom(type) && daoSchemaClass != null) {
			try {
				RequestableInject instance = getRequestableInjector(request,Connection.class);
				instance.destroy(Connection.class);
			}catch (Throwable e) {
				System.out.format("[RequestableService] ERROR destroying IDaoSchema at %s, Detail:%s\n",service.getClass().getName(),e.getMessage());
			}
		}
		List<Field> fields = UtilReflection.getAllAnnotatedFields(type, Injected.class);
		
		for(Field f:fields) {
			try {
				RequestableInject instance = getRequestableInjector(request,f.getType());
				instance.destroy(f.getType());
			}catch(Throwable e) {
				System.out.format("[RequestableService] ERROR destroying @Injected field:%s.%s\n",type.getName(),f.getName());
				e.printStackTrace();
			}
		}
	}
	private void injectDependencies(HttpServletRequest request, Object service) throws ConnectionManagerException, SQLException, UnknownHostException {
		Class<?> type = service.getClass();
		
		if (RequiereSession.class.isAssignableFrom(type)) {
			((RequiereSession) service).setSession(request.getSession());
		}
		if (RequiereDaoSchema.class.isAssignableFrom(type) && daoSchemaClass != null) {
			try {
				RequestableInject instance = getRequestableInjector(request,Connection.class);
				Constructor<? extends IDaoSchema> dao = daoSchemaClass.getConstructor(Connection.class);
				((RequiereDaoSchema) service).setDaoSchema(dao.newInstance(instance.create(Connection.class)));
			}catch (Throwable e) {
				System.out.format("[RequestableService] ERROR setting IDaoSchema at %s, Detail:%s\n",service.getClass().getName(),e.getMessage());
			}
		}
		if (RequiereEnviorement.class.isAssignableFrom(type)) {
			((RequiereEnviorement) service).setClientAddress(ServletUtil.getClientAddress(request));
			((RequiereEnviorement) service).setClientAgent(request.getHeader("user-agent"));
			((RequiereEnviorement) service).setServerAddress(ServletUtil.getServerAddress(request));

		}
		
		List<Field> fields = UtilReflection.getAllAnnotatedFields(type, Injected.class);	
		for(Field f:fields) {
			try {
				f.setAccessible(true);
				RequestableInject instance = getRequestableInjector(request,f.getType());
				f.set(service, instance.create(f.getType()));
			}catch(Throwable e) {
				System.out.format("[RequestableService] ERROR setting @Injected field:%s.%s\n",type.getName(),f.getName());
				e.printStackTrace();
			}
		}

	}

	private RequestableInject getRequestableInjector(HttpServletRequest request,Class<?> type) throws InstantiationException, IllegalAccessException {
		String key = RequestableInject.class.getName()+"."+type.getName();
		RequestableInject instance = (RequestableInject) request.getAttribute(key);
		if(instance == null) {
			instance = new RequestableInject();
			request.setAttribute(key,instance);
		}
		return instance;
	}


	private void removeOnProgress(String idCurrentOperation) {
		synchronized (sync) {
			try {
				onProgress.remove(onProgress.indexOf(idCurrentOperation));
			} catch (IndexOutOfBoundsException e) {
			}
		}
	}

	private void addOnProgress(String idCurrentOperation, Method operation) throws RequestableException {
		synchronized (sync) {
			if (onProgress.contains(idCurrentOperation)) {
				RequestableOperation annotation = operation.getAnnotation(RequestableOperation.class);
				if (!annotation.allowMultipleRequest())
					throw new RequestableException(
							String.format("La operación:%s no permite múltiples request.", annotation.identifier()),
							RequestableException.REQUEST_ALREADY_ON_PROCESS);

			} else
				onProgress.add(idCurrentOperation);
		}
	}

	private String getTokenCurrentOperation(HttpServletRequest request) {

		StringBuilder params = new StringBuilder("{\"JSESSIONID\":\"");
		params.append(request.getSession().getId()).append("\"");
		Enumeration<String> par = request.getParameterNames();
		while (par.hasMoreElements()) {
			String param = par.nextElement();
			if (request.getParameter(param).startsWith("{") || request.getParameter(param).startsWith("["))
				params.append(String.format(",\"%s\":%s", param, request.getParameter(param)));
			else
				params.append(String.format(",\"%s\":\"%s\"", param, request.getParameter(param)));
		}
		params.append("}");

		return params.toString();
	}

}
