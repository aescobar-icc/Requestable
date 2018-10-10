package beauty.requestable.core;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beauty.requestable.util.serialize.JSONUtil;



/**
 * Servlet implementation class JSInitData
 */
@WebServlet("/js/beauty/requestable/initdata.js")
public class RequestableInitData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String INIT_DATA_KEY = "beauty.requestable.initdata";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RequestableInitData() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("application/x-javascript");
		response.setCharacterEncoding("UTF-8");
	    response.addHeader("Cache-Control", "no-cache");
	    response.addHeader("Cache-Control", "no-store");
	    
	    StringBuilder strb = new StringBuilder("(function(){");
	    strb.append("window.beauty = window.beauty || {};");
	    strb.append("var b = window.beauty;");
	    strb.append("b.default = {page:{HTTP_ERROR_404:'beauty.requestable.core.constants.page.HTTP_ERROR_404'}};");
	    strb.append("b.server = {};");
	    strb.append("b.server.host = \"").append(ServletUtil.getServerAddress(request)).append("\";");
	    String allInitData = JSONUtil.encodeJsonString(getData(request));
	    strb.append("b.initData = ").append(allInitData).append(";");
	    strb.append("b.allInitData = ").append(allInitData).append(";");
	    
	    strb.append("b.uri = location.href.replace(b.server.host,'');");
	    strb.append("b.initData = b.initData[b.uri];");
	    
	    strb.append("if(!b.initData){var uri = b.uri[b.uri.length -1] == '/'?b.uri.substring(0,b.uri.length -1):b.uri;for(var i in b.default.page){var def =b.default.page[i]; if(b.allInitData[def] && b.allInitData[def].page.uri == uri){ b.initData = b.allInitData[def];break;}}}");
	    
	    strb.append("$(function(){ if(b.initData){$('[data-init-bind]').each(function(){var $e=$(this);attr=$e.attr('data-init-bind');if($e.hasAttribute('data-component-id')){$e.bindObject(b.initData[$e.attr('data-component-id')]);}else{$e.bindObject(b.initData.page);}});}});");
	    
	    strb.append("})();");
	    
	    byte[] bytes = strb.toString().getBytes();
	    ServletOutputStream outputStream = response.getOutputStream();
	 	outputStream.write(bytes, 0, bytes.length);
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@SuppressWarnings("unchecked")
	public static void addData(HttpServletRequest request,String uri,String id,Object value) {
		HttpSession sess = request.getSession(false);


		HashMap<String,Object> map = (HashMap<String, Object>) sess.getAttribute(INIT_DATA_KEY);
		if(map == null) {
			map = new HashMap<String,Object>();
			sess.setAttribute(INIT_DATA_KEY,map);
		}

		HashMap<String,Object> values = (HashMap<String, Object>) map.get(uri);
		if(values == null) {
			values = new HashMap<String,Object>();
			map.put(uri,values);
		}
		
		values.put(id,value);
	}
	public static HashMap<String, Object> getData(HttpServletRequest request) {
		HttpSession sess = request.getSession(false);
		
		@SuppressWarnings("unchecked")
		HashMap<String,Object> map = (HashMap<String, Object>) sess.getAttribute(INIT_DATA_KEY);
		if(map == null) {
			map = new HashMap<String,Object>();
			sess.setAttribute(INIT_DATA_KEY,map);
		}
		return map;
	}
	public static void clearData(HttpServletRequest request) {
		HttpSession sess = request.getSession(false);
		sess.removeAttribute(INIT_DATA_KEY);
	}
}
