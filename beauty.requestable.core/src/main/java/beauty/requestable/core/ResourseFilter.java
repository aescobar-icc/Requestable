package beauty.requestable.core;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beauty.requestable.core.constants.OperationDefaults;
import beauty.requestable.core.constants.PageDefaults;
import beauty.requestable.core.enums.RequestableType;
import beauty.requestable.core.wrappers.WrapperServletResponse;
import beauty.requestable.util.sql.postgres.schema.DaoPostgresSchema;


/**
 * Servlet Filter implementation class ResourceFilter
 */
@WebFilter("/*")
public class ResourseFilter implements Filter {
    /**
     * Default constructor. 
     */
    public ResourseFilter() {
        // TODO Auto-generated constructor stub
    }
    
    /**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {	
		ServletUtil.setServletContext(config.getServletContext());

		try {
			ServletUtil.getRequestableXML();
		} catch (Throwable e) {
			//e.printStackTrace();
		}
		class Worker implements Runnable {
			private Thread thread;
			public void setThread(Thread thread) {
				this.thread = thread;
			}
			@Override
			public void run() {
				// load all requetable class
				System.setProperty("ServletUtil.showDebug", "on");
				ServletUtil.findAllRequestableClass();
				resolveDataBaseSchema();
				
				
				thread.interrupt();
			}
		}
		Worker w = new Worker();
		Thread requestableFinder = new Thread(w);
		w.setThread(requestableFinder);
		requestableFinder.start();
	
	}

	private void resolveDataBaseSchema() {
		try {
			RequestableInject inject = new RequestableInject();
			Connection conn = inject.create(Connection.class);

			DatabaseMetaData meta = conn.getMetaData();

			switch (meta.getDatabaseProductName()) {
				case "PostgreSQL":
					RequestableService.daoSchemaClass = DaoPostgresSchema.class;
					break;
	
				default:
					System.err.println("DATA BASE PROVIDER:" + meta.getDatabaseProductName() + " NOT SUPPORTED");
					break;
			}
			inject.destroy(Connection.class);
		}catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		System.out.println("Filter destroy");
	}	

	private boolean acceptsGZipEncoding(HttpServletRequest httpRequest) {
		String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
		return acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1;
	}
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest  req  = (HttpServletRequest)request;	
		HttpServletResponse resp = (HttpServletResponse)response;	

		String contextPath = req.getContextPath();
		String requestURI  = req.getRequestURI().replace(contextPath, "");
		if(!"/".equals(requestURI) && requestURI.endsWith("/")) {
			requestURI = requestURI.replaceAll("\\/$", "");
		}
		//System.out.println("ContextPath:"+contextPath+" Request URI:"+requestURI);
		
		//HttpSession session = req.getSession(true);
		
		
		Class<?> serviceClass = ServletUtil.getRequestableClass(requestURI, RequestableType.WEB_PAGE);
		if(serviceClass != null) {
			//response.getWriter().write("web page found!!!");
			forwardPage(request, response, requestURI,null,serviceClass);
		}else {
			serviceClass = ServletUtil.getFromMultiUrl(request,requestURI);
			if(serviceClass != null) {
				//response.getWriter().write("web page found!!!");
				forwardPage(request, response, requestURI,null,serviceClass);
			}else{
				//chain.doFilter(request, response);
				doFilterChain(req, resp, chain,requestURI);
				//response.getWriter().write("resource requested not found!!!");
			}
		}
	}
	private void forwardPage(ServletRequest request, ServletResponse response,String requestURI,HashMap<String, Object> params,Class<?> requestable) throws ServletException, IOException {
		request.setAttribute(RequestableService.ATTR_CLASS,requestable);
		request.setAttribute("identifier",requestURI);
		request.setAttribute("operation",OperationDefaults.WEB_PAGE_INIT);
		request.setAttribute("type",RequestableType.WEB_PAGE);
		request.setAttribute("parameters",params);
		RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/beauty.requestable.service");
		dispatcher.forward(request,response);
	}
	private void doFilterChain(HttpServletRequest request, HttpServletResponse response, FilterChain chain,String requestURI) throws IOException, ServletException{
			
		try {
			WrapperServletResponse wresponse = new WrapperServletResponse(response);
			chain.doFilter(request, wresponse);

			
			if(wresponse.getStatus() == 200) {
				Collection<String> names = wresponse.getHeaderNames();
				for(String name:names){
					response.setHeader(name, wresponse.getHeader(name));
					System.out.format("%s:%s \n",name,wresponse.getHeader(name));
				}
	
				
				byte[] writtenBytes = wresponse.getOutputStreamWrittenBytes();
				ServletOutputStream out 	= null;
				PrintWriter 		writer	= null;
				if(writtenBytes!= null){
					//System.out.println(" -- written bytes:" +writtenBytes.length);
					out = response.getOutputStream();
					out.write(writtenBytes);
				}else{
					String text = wresponse.getPrinterWrittenText();
					if(text != null){
						//System.out.println(" -- written text:");
						writer = response.getWriter();
						writer.print(text);
					}
				}
				if(out != null){
					out.flush();
					out.close();
				}
			    if(writer != null){
			    	writer.flush();
			    	writer.close();
			    }
			}else {
				int sc = wresponse.getStatus();
				String error = wresponse.getErrorMsg();
				if(error == null) {
					error = "ERROR "+sc+" processing "+requestURI;
				}
				response.setStatus(sc);
				String uriErrorPage = getDefaultErrorPage(); 
				if(uriErrorPage != null) {
					HashMap<String, Object> params = new HashMap<>();
					params.put("errMsg", error);
					params.put("errCode", sc);
					params.put("uri", requestURI);
					forwardPage(request, response, uriErrorPage,params,null);
				}else {
					response.setContentType("text/html; charset=UTF-8");
					response.getWriter().print("Error:"+error);
				}
			}
			
		}catch (Throwable e) {
			response.getWriter().print("Error:500");
			e.printStackTrace();
		}
	}

	/*
	private void registerBasicResources(ServletContext context) {
		
		servletsUrlPatterns = new ArrayList<String>();
		String[] classPath = new String[2];
		classPath[0] = context.getRealPath("WEB-INF/lib");
		classPath[1] = context.getRealPath("WEB-INF/classes");
		
		/**
		 * Register Servlets
		 *
		List<Class<?>> srvlts = Reflection.listAnnotadedClass(WebServlet.class,classPath);		
		WebServlet wservlet;
		for (Class<?> clss : srvlts) {
			wservlet = clss.getAnnotation(WebServlet.class);
			
			for(String url:wservlet.value())
				servletsUrlPatterns.add(url);
		}
		/**
		 * Register Controllers
		 *
		List<Class<?>> controllers = Reflection.listAnnotadedClass(WebController.class,classPath);
		WebController controller;
		for (Class<?> clss : controllers) {
			controller = clss.getAnnotation(WebController.class);
			BaseController.register(controller.id(), clss);
		}

		/**
		 * Register Welcome Page Controller
		 *
		String[] welcomeFiles = (String[]) context.getAttribute("org.apache.catalina.WELCOME_FILES");
		for(String wp:welcomeFiles){
			wp = wp.replace("/WEB-INF","");
			Class<?> clss = PageController.getControllerClassByRender(wp);
			if(clss != null){
				welcomePage = clss;
				break;
			}
			
		}
		
		
	}*/

	private String getDefaultErrorPage() {
		Class<?> serviceClass = ServletUtil.getRequestableClass(PageDefaults.HTTP_ALL_ERROR, RequestableType.WEB_PAGE);
		if(serviceClass != null)
			return PageDefaults.HTTP_ALL_ERROR;
		serviceClass = ServletUtil.getRequestableClass(PageDefaults.HTTP_ERROR_404, RequestableType.WEB_PAGE);
		if(serviceClass != null)
			return PageDefaults.HTTP_ERROR_404;
		
		return null;
	}


}
