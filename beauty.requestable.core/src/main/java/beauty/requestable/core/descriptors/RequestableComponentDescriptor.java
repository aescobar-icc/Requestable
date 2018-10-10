package beauty.requestable.core.descriptors;

import java.util.ArrayList;
import java.util.List;

public class RequestableComponentDescriptor {
	
	private String html = "";
	private List<String> js = new ArrayList<String>();
	private List<String> css = new ArrayList<String>();
	private Class<?> componentClass = null;
	
	
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public List<String> getJs() {
		return js;
	}
	public void setJs(List<String> js) {
		this.js = js;
	}
	public List<String> getCss() {
		return css;
	}
	public void setCss(List<String> css) {
		this.css = css;
	}
	public Class<?> getComponentClass() {
		return componentClass;
	}
	public void setComponentClass(Class<?> componentClass) {
		this.componentClass = componentClass;
	}

	
	
}
