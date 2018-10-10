package beauty.requestable.util.serialize.parser;

import javax.json.JsonObject;

public class JSONFieldInfo {
	
	public JSONFieldInfo(String objectName, String jsonName, Object jsonValue, JsonObject jsonSource) {
		this.objectName = objectName;
		this.jsonName = jsonName;
		this.jsonValue = jsonValue;
		this.jsonSource = jsonSource;
	}
	
	private String objectName;
	private String jsonName;
	private Object jsonValue;
	private JsonObject jsonSource;
	
	public String getObjectName() {
		return objectName;
	}
	public String getJsonName() {
		return jsonName;
	}
	public Object getJsonValue() {
		return jsonValue;
	}
	public JsonObject getJsonSource() {
		return jsonSource;
	}
	public void removeJsonField(String key) {
		jsonSource.remove(key);
	}
	
	
}
