package beauty.requestable.util.serialize;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import beauty.requestable.util.format.UtilStringFormat;
import beauty.requestable.util.reflection.UtilReflection;
import beauty.requestable.util.serialize.annotations.JSONField;
import beauty.requestable.util.serialize.annotations.JSONFieldIndex;
import beauty.requestable.util.serialize.annotations.JSONIgnore;
import beauty.requestable.util.serialize.annotations.JSONSource;
import beauty.requestable.util.serialize.parser.JSONFieldInfo;
import beauty.requestable.util.serialize.parser.JSONFieldParser;

public class JSONUtil {
	private static final String TO_PACKAGE = "cl.pcFactory.negocio.to.";
	private static final HashMap<Class<?>,HashMap<String, List<Field>>> JSON_OBJ_FIELD_MAP = new HashMap<Class<?>,HashMap<String, List<Field>>>();
	private static final HashMap<Class<?>,Field> JSON_SOURCE_FIELD = new HashMap<Class<?>,Field>();

	public static JsonObject getJsonObject(String jsonVal) {
		JsonReader reader = Json.createReader(new StringReader(jsonVal));
		return reader.readObject();
	}
	public static JsonArray getJsonArray(String jsonVal) {
		return getJsonObject(jsonVal).asJsonArray();
	}
	public static String encodeJsonString(Object object){
			HashMap<Object,JsonValue> serialized = new HashMap<Object,JsonValue>();
			JsonValue o = encodeJsonObject(object,serialized);
			return String.valueOf(o);
			/*try {
				return new String(o.toString().getBytes(),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;*/
	}
	public static <K,V> HashMap<K,V> decodeHash(String jsonArrayString,Class<K> keyType,Class<V> valueType) throws JSONException {
		try{
			if(jsonArrayString == null || jsonArrayString.equals(""))
				return null;
			JsonObject obj = getJsonObject(jsonArrayString);
			if(obj == null)
				return null;
			if(obj instanceof JsonArray)
				return decodeHash((JsonArray)obj, keyType, valueType);
			return decodeHash((JsonObject)obj, keyType, valueType);
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeHash of %s", keyType.getName()), e);
		}
	}
	public static <K,V> HashMap<K,V> decodeHash(JsonArray array,Class<K> keyType,Class<V> valueType) throws JSONException {
		try{			
			return decodeHash(array.asJsonObject(), keyType, valueType);
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeHash of %s", keyType.getName()), e);
		}
	}
	public static <K,V> HashMap<K,V> decodeHash(JsonObject jsonObject,Class<K> keyType,Class<V> valueType) throws JSONException {
		try{			
			HashMap<K,V> 		hash	= new HashMap<K,V>();
			Set<String> keys = jsonObject.keySet();
			for(String k: keys){
				K key = parseObject(getJsonObject(k), keyType);
				V value = parseObject(jsonObject.get(k),valueType );
				hash.put(key, value);
			}
			return hash;
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeHash of %s", keyType.getName()), e);
		}
	}

	public static Object decodeArray(String jsonArrayString, Class<?> parameterType) throws JSONException {
		Object array = null;
		try{
			if(jsonArrayString == null || jsonArrayString.equals(""))
				return null;
			
			
			JsonArray	jsonArray	= getJsonArray(jsonArrayString);
			int size = jsonArray.size();
			if(parameterType.isArray()) {
				Class<?> cls = parameterType.getComponentType();
				if(cls.isPrimitive()) {
					if(boolean.class.isAssignableFrom(cls)) {
						boolean[] ar1 = new boolean[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), boolean.class);
						}
						array = ar1;
					}else if(byte.class.isAssignableFrom(cls)) {
						byte[] ar1 = new byte[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), byte.class);
						}
						array = ar1;
					}else if(char.class.isAssignableFrom(cls)) {
						char[] ar1 = new char[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), char.class);
						}
						array = ar1;
					}else if(short.class.isAssignableFrom(cls)) {
						short[] ar1 = new short[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), short.class);
						}
						array = ar1;
					}else if(int.class.isAssignableFrom(cls)) {
						int[] ar1 = new int[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), int.class);
						}
						array = ar1;
					}else if(long.class.isAssignableFrom(cls)) {
						long[] ar1 = new long[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), long.class);
						}
						array = ar1;
					}else if(float.class.isAssignableFrom(cls)) {
						float[] ar1 = new float[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), float.class);
						}
						array = ar1;
					}else if(double.class.isAssignableFrom(cls)) {
						double[] ar1 = new double[size];
						for(int i=0;i<size;i++){
							ar1[i] = parseObject(jsonArray.get(i), double.class);
						}
						array = ar1;
					}					
				}else {
					Object[] ar1 = UtilReflection.createArray(cls, size);
					for(int i=0;i<size;i++){
						ar1[i] = parseObject(jsonArray.get(i), cls);
					}
					array = ar1;
				}
			}
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decode Array of :%s",parameterType.getName()), e);
		}
		return array;
	}
	public static <T> List<T> decodeList(String jsonArrayString,Class<T> listPararameterType) throws JSONException {
		try{
			if(jsonArrayString == null || jsonArrayString.equals(""))
				return null;
			
			JsonArray	array	= getJsonArray(jsonArrayString);
			return decodeList(array, listPararameterType);
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeList of :%s",listPararameterType.getName()), e);
		}
	}
	public static List<?> decodeList(String jsonArrayString,Type listPararameterType) throws JSONException {
		try{
			if(jsonArrayString == null || jsonArrayString.equals(""))
				return null;

			JsonArray array	= getJsonArray(jsonArrayString);
			return decodeList(array, listPararameterType);
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeList of TYPE :%s",listPararameterType.getTypeName()), e);
		}
	}
	public static <T> List<T> decodeList(JsonArray	array,Class<T> listPararameterType) throws JSONException {
		try{			
			List<T>		list	= new ArrayList<T>();
			List<Field> anns = UtilReflection.getAllAnnotatedFields(listPararameterType, JSONFieldIndex.class);
			Field fieldIndex = null;
			if(anns.size() > 0) {
				fieldIndex = anns.get(0);
			}
			int i=0;
			for(JsonValue jObj:array){
				T instance = parseObject(jObj, listPararameterType);
				if(fieldIndex != null) {
					UtilReflection.setFieldValue(fieldIndex, instance, i);// set index of object in array
				}
				list.add(instance);
				i++;
			}		
			return list;
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeList of %s",listPararameterType.getName()), e);
		}
	}
	public static List<?> decodeList(JsonArray	array,Type listPararameterType) throws JSONException {
		try{			
			List<Object>	 list	= new ArrayList<Object>();
			List<Field> anns = UtilReflection.getAllAnnotatedFields((Class<?>)listPararameterType, JSONFieldIndex.class);
			Field fieldIndex = null;
			if(anns.size() > 0) {
				fieldIndex = anns.get(0);
			}
			int i=0;
			for(JsonValue jObj:array){
				Object instance = parseObject(jObj, (Class<?>)listPararameterType);
				if(fieldIndex != null) {
					UtilReflection.setFieldValue(fieldIndex, instance, i);// set index of object in array
				}
				list.add(instance);
				i++;
			}		
			return list;
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeList of TYPE %s",listPararameterType.getTypeName()), e);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T decodeObject(String jsonObjectString, Class<T> cls) throws JSONException{
		try{
			if(jsonObjectString == null)
				return null;
			if(jsonObjectString.equals("")){
				if(String.class.isAssignableFrom(cls))
					return (T) jsonObjectString;
				return null;
			}
	
			if(UtilReflection.isNativeType(cls))
				return UtilReflection.parseToNative(cls, jsonObjectString);
			
			JsonValue obj = getJsonObject(jsonObjectString);
			if(obj == null)
				return null;
			
			return parseObject(obj, cls);
		}catch(Throwable e){
			throw new JSONException(String.format("Error: decodeObject %s",cls.getName()), e);
		}
	}
	/**
	 * 
	 * @param jsonObj
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> T parseObject(JsonValue obj,Class<T> type) throws Exception{
		if(UtilReflection.NATIVE_TYPES.contains(type) || UtilReflection.WRAPPER_NATIVE_TYPES.contains(type)){
			return  UtilReflection.parseToNative(type, String.valueOf(obj));
		}else{
			JsonObject jsonObj = obj.asJsonObject();
			if(jsonObj.get("jsonCompatibleType") != null){
				Class<?> compatipleType =  Class.forName(TO_PACKAGE+jsonObj.get("jsonCompatibleType"));
				if(type.isAssignableFrom(compatipleType)){
					type = (Class<T>) compatipleType;
				}
			}
			T instance = type.newInstance();
			Object[] keys = jsonObj.keySet().toArray();
			for(Object jsonName: keys){
				try{
					if(jsonName.equals("serialVersionUID"))
							continue;
					Field field = null;
					if(UtilReflection.containsField((String)jsonName, type)) {
						field = UtilReflection.getField((String)jsonName, type);
						setField(instance, field, jsonObj.get(jsonName));
					}else {
						String jsonName2 = UtilStringFormat.underToCamel((String)jsonName);
						if(UtilReflection.containsField(jsonName2, type)) {
							field = UtilReflection.getField(jsonName2, type);
							setField(instance, field, jsonObj.get(jsonName));
						}else {// verifica si el objeto tiene asociados origen json
							HashMap<String, List<Field>> jsonMap = JSON_OBJ_FIELD_MAP.get(type);
							if(jsonMap == null) {// si cache de campos asociados al objecto no existe la crea
								jsonMap = new HashMap<String, List<Field>>();
								JSON_OBJ_FIELD_MAP.put(type,jsonMap);
								List<Field> allClassFields = UtilReflection.getAllFields(type);
								for(Field classField:allClassFields) {
									//agrega a cache los origenes json admitidos para field
									if(classField.isAnnotationPresent(JSONField.class)) {
										JSONField ann = classField.getAnnotation(JSONField.class);
										String[] jsonAdmitedNames = ann.jsonName();
										for(String jsonAdmitedName:jsonAdmitedNames) {
											List<Field> ff = jsonMap.get(jsonAdmitedName);
											if(ff == null) {
												ff = new ArrayList<>();
												jsonMap.put(jsonAdmitedName,ff);
											}
											System.out.println("[JSONUtil] INFO binding objectField:"+jsonAdmitedName+" width objectField:"+type.getName()+"."+classField.getName());
											ff.add(classField);
										}
									}
									//agrega a cache el field que guarda json data original
									if(classField.isAnnotationPresent(JSONSource.class)) {
										if(classField.getType().isAssignableFrom(String.class)) {
											JSON_SOURCE_FIELD.put(instance.getClass(),classField);
										}else {
											System.err.println(String.format("[JSONUtil.parseObject] WARNING parsing field:%s, JSONStore annotated field must be String",jsonName));
										}
									}
								}
							}
							Field jsonSourceField = JSON_SOURCE_FIELD.get(type);
							List<Field> fields = jsonMap.get(jsonName);
							if(fields != null) {
								for(Field fieldType:fields) {
									JSONField ann = fieldType.getAnnotation(JSONField.class);
									JsonValue jsonValue = jsonObj.get(jsonName);
									if(ann.parse() && instance instanceof JSONFieldParser) {
										JSONFieldParser<?> parser = (JSONFieldParser<?>)instance;
										JSONFieldInfo f = new JSONFieldInfo(fieldType.getName(),(String) jsonName, jsonValue, jsonObj);
										//jsonValue = ;
										//val = UtilReflection.callMethod(instance, ann.parserMethod(),jsonObj.get(fieldName));
										Object objVal = parser.parse(f);
										UtilReflection.setFieldValue(fieldType, instance, objVal);
									}else
										setField(instance, fieldType, jsonValue);
								}
							}
							if(jsonSourceField != null) {
								UtilReflection.setFieldValue(jsonSourceField, instance, jsonObj.toString());
							}
						}
					}
				}catch(Throwable e){
					Throwable cause = UtilReflection.getRootCause(e);
					System.err.println(String.format("[JSONUtil.parseObject] ERROR parsing field:%s caused by: %s",jsonName,cause.toString()));
				}
			}
			return instance;
		}
	}
	private static void setField(Object instance,Field field,JsonValue value) throws Exception{
		//System.out.println(String.format("parsing %s=%s --> %s", field.getName(),value,field.getType()));
		if(value == null){
			UtilReflection.setFieldValue(field, instance, null);
		}else{
			Class<?> type = field.getType();
			if(Collection.class.isAssignableFrom(type)){
				Type parameterType = UtilReflection.getParameterTypes(field)[0];
				Collection<Object> list = new ArrayList<Object>();
				JsonArray array =(JsonArray)value;
				for(JsonValue jObj:array){
					Object object = parseObject(jObj, (Class<?>) parameterType);
					list.add(object);
				}
				UtilReflection.setFieldValue(field, instance, list);
			}else if(type.isArray()){
				Object[] valArray = null;
				JsonArray array =(JsonArray)value;
				Type parameterType = type.getComponentType();
				if (type instanceof Class) {
					valArray = UtilReflection.createArray((Class<?>) parameterType, array.size());
					int i = 0;
					for(JsonValue jObj:array){
						Object object = parseObject(jObj, (Class<?>) parameterType);
						valArray[i++]= object;
					}
				}
				UtilReflection.setFieldValue(field, instance,valArray);
				
			}else if(Map.class.isAssignableFrom(type)){
				ParameterizedType pt = (ParameterizedType) field.getGenericType();
				Class<?>[] cls = UtilReflection.getParameterClass(pt);
				Class<?> keyType = cls[0];
				Class<?> valueType = cls[1];
				
				if( JsonObject.class.isAssignableFrom(value.getClass()) ){
					UtilReflection.setFieldValue(field, instance, decodeHash((JsonObject)value, keyType, valueType));
				}else {
					UtilReflection.setFieldValue(field, instance, decodeHash((JsonArray)value, keyType, valueType));
				}
				//return encodeJsonMap((Map<?, ?>)object);
			}else if(Enum.class.isAssignableFrom(type)){
				//return createJsonObject(object);
			}else{
				UtilReflection.setFieldValue(field, instance, parseObject(value,field.getType()));
			}
		}
	}
	private static JsonArray encodeJsonList(Collection<?> coll,HashMap<Object,JsonValue> serialized){
		JsonArrayBuilder buider = Json.createArrayBuilder();
		for(Object o:coll){
			buider.add(encodeJsonObject(o,serialized));
		}
		return buider.build();
	}
	private static JsonArray encodeJsonArray(Object[] array,HashMap<Object,JsonValue> serialized){
		JsonArrayBuilder buider = Json.createArrayBuilder();
		for(Object o:array){
			buider.add(encodeJsonObject(o,serialized));
		}
		return buider.build();
	}
	private static JsonObject encodeJsonMap(Map<?, ?> map,HashMap<Object,JsonValue> serialized){
		JsonObjectBuilder buider = Json.createObjectBuilder();	
		Object o;
		for(Object key:map.keySet()){
			o = map.get(key);
			if(o == null)
				buider.addNull(String.valueOf(key));
			else
				buider.add(String.valueOf(key), encodeJsonObject(o,serialized));
		}
		return buider.build();
	}

	private static JsonObject createJsonObject(Object object,HashMap<Object,JsonValue> serialized){
		JsonObjectBuilder buider = Json.createObjectBuilder();	
		List<Field> fields;
		if(Throwable.class.isAssignableFrom(object.getClass()))
			fields = Arrays.asList(object.getClass().getDeclaredFields());
		else
			fields = UtilReflection.getAllFields(object.getClass(),true);
		for(Field field:fields){
			try{
				if(field.getName().equals("serialVersionUID"))
					continue;//ignora los campos anotados
				if(field.isAnnotationPresent(JSONIgnore.class))
					continue;//ignora los campos anotados
				if (field.getType().isAssignableFrom(char.class)) {
					String value = String.valueOf(UtilReflection.getFieldValue(field, object));
					if(value == null)
						buider.addNull(field.getName());
					else
						buider.add(field.getName() ,value);	
				}else {
					JsonValue value = encodeJsonObject(UtilReflection.getFieldValue(field, object),serialized);
					if(value == null)
						buider.addNull(field.getName());
					else
						buider.add(field.getName() ,value);	
				}
					
			}catch(Throwable e){
				e.printStackTrace();
				buider.addNull(field.getName());
				//System.out.println(String.format("[JSONUtil.encodeJsonObject] Imposible encode %s.%s",object.getClass().getSimpleName(),field.getName()));
			}
		}
		return buider.build();
	}
	private static JsonValue encodeJsonObject(Object object,HashMap<Object,JsonValue> serialized){
		if(object != null && !object.getClass().isAnnotationPresent(JSONIgnore.class)){
			if(!serialized.containsKey(object)) {
				if(Collection.class.isAssignableFrom(object.getClass())){
					return encodeJsonList((Collection<?>)object,serialized);
				}else if(object.getClass().isArray()){
					Object[] array = UtilReflection.convertToArray(object);
					return encodeJsonArray(array,serialized);
				}else if(Map.class.isAssignableFrom(object.getClass())){
					return encodeJsonMap((Map<?, ?>)object,serialized);
				}else if(Enum.class.isAssignableFrom(object.getClass())){
					return createJsonObject(object,serialized);
				}else{
		
					if(UtilReflection.isPosibleNativeType(object.getClass())){
						/*if(		object.getClass() == DateTime.class
								|| object.getClass() == java.sql.Date.class 
								|| object.getClass() == java.util.Date.class 
								|| object.getClass() == java.sql.Timestamp.class 
								|| object.getClass() == java.sql.Time.class 
								|| object.getClass() == StackTraceElement.class)
							return String.valueOf(object);*/
						JsonObjectBuilder buider = Json.createObjectBuilder();	
						buider.add("value", String.valueOf(object));
						
						/*if(UtilReflection.isNumericType(object.getClass())){
							return buider.build().getJsonNumber("value");
						}else */if(UtilReflection.isBooelanType(object.getClass())){
							if((Boolean)object == true)
								return JsonValue.TRUE;
							return JsonValue.FALSE;
						}else{
							return buider.build().getJsonString("value");
						}
					}
					JsonValue json = createJsonObject(object,serialized);
					serialized.put(object,json);
					return json;
				}
			}else {
				return serialized.get(object);
			}
		}
		return null;
	}
}
