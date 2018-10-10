package beauty.requestable.util.reflection;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import beauty.requestable.util.date.DateFormat;
import beauty.requestable.util.date.DateTime;
import beauty.requestable.util.file.FileUtil;
import beauty.requestable.util.serialize.JSONUtil;
import beauty.requestable.util.sql.annotations.JSONColumn;

/**
 * 
 * @author Adan Escobar
 * @email aescobar.icc@gmail.com
 */
public class UtilReflection {
	public static final Set<Class<?>> NATIVE_TYPES = getNativeTypes();
	public static final Set<Class<?>> NUMERIC_TYPES = getNumericTypes();
		public static final Set<Class<?>> WRAPPER_NATIVE_TYPES = getWrapperNativeTypes();
		public static final HashMap<Class<?>,Class<?>> NATIVE_TYPE_BY_WRAPPER = getNativeTypesByWrapper();
	private static final String CLASS_EXTENSION = ".class";
	private static final String FILE_SEPARATOR = File.separator.equals("\\")?"\\\\":File.separator;		
	private static final String BASE_URI = String.format("WEB-INF%sclasses%s",FILE_SEPARATOR,FILE_SEPARATOR);

	/**
	 * Checks if type:<br/>
	 * -- Is equal to some java native type.<br/> 
	 * -- Is in some java native package.<br/>
	 * -- Is enumeration.<br/>
	 * @param clss
	 * @return
	 */
 	public static boolean isPosibleNativeType(Class<?> clss) {
        return  NATIVE_TYPES.contains(clss) 	|| 
        		WRAPPER_NATIVE_TYPES.contains(clss) 	|| 
        		isPosibleNativePackage(clss.getName()) || 
        		clss.isEnum();
    }
 	public static boolean isNativeType(Class<?> clss) {
        return  NATIVE_TYPES.contains(clss) 	|| 
        		WRAPPER_NATIVE_TYPES.contains(clss) ;
    }
 	public static boolean isWrapper(Class<?> clss){
        return  WRAPPER_NATIVE_TYPES.contains(clss);
    }
    
    public static boolean isPosibleNativePackage(String className) {
       return 	className.startsWith("com.oracle") ||
		        className.startsWith("com.sun") ||
		        className.startsWith("java.") ||
		        className.startsWith("javax.") ||
		        className.startsWith("sun.") ||
		        className.startsWith("sunnw.") ||
		        className.startsWith("org.");
	}

	private static Set<Class<?>> getWrapperNativeTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        return ret;
    }

	private static Set<Class<?>> getNativeTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();

        ret.add(boolean.class);
        ret.add(char.class);
        ret.add(byte.class);
        ret.add(short.class);
        ret.add(int.class);
        ret.add(long.class);
        ret.add(float.class);
        ret.add(double.class);
        ret.add(void.class);
        ret.add(BigDecimal.class);
        ret.add(BigInteger.class);
        ret.add(String.class);
        
        ret.add(DateTime.class);
        ret.add(java.sql.Date.class);
        ret.add(java.sql.Timestamp.class);
        ret.add(java.util.Date.class);
        ret.add(java.sql.Time.class);
        return ret;
    }
	private static Set<Class<?>> getNumericTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();

        ret.add(byte.class);
        ret.add(short.class);
        ret.add(int.class);
        ret.add(long.class);
        ret.add(float.class);
        ret.add(double.class);
        
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        
        ret.add(BigDecimal.class);
        ret.add(BigInteger.class);
        
        return ret;
    }
	private static HashMap<Class<?>,Class<?>> getNativeTypesByWrapper(){
		HashMap<Class<?>,Class<?>> ret = new HashMap<Class<?>, Class<?>>();

        ret.put(Boolean.class, boolean.class);
        ret.put(Character.class, char.class);
        ret.put(Byte.class,byte.class);
        ret.put(Short.class,short.class);
        ret.put(Integer.class,int.class);
        ret.put(Long.class,long.class);
        ret.put(Float.class,float.class);
        ret.put(Double.class,double.class);
        ret.put(Void.class,void.class);
        
        return ret;
    }

	public static Class<?>  getTypesByWrapper(Class<?> type){
		return NATIVE_TYPE_BY_WRAPPER.get(type);
	}
    
    public static Object getFieldValue(Field field, Object obj) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException{
		Method getter;
		field.setAccessible(true);
		if(field.isAccessible())
			return field.get(obj);
		if(field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)){
			try{
				getter = obj.getClass().getMethod("is"+field.getName().substring(0, 1).toUpperCase()+field.getName().substring(1));
			}catch(Throwable e){
				getter = obj.getClass().getMethod("get"+field.getName().substring(0, 1).toUpperCase()+field.getName().substring(1));
			}
		}else
			getter = obj.getClass().getMethod("get"+field.getName().substring(0, 1).toUpperCase()+field.getName().substring(1));
		return getter.invoke(obj);
    }
	public static Object getFieldValue(String fieldName, Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		Field field = getField(fieldName, obj.getClass());
		return getFieldValue(field, obj);
	}   
	@SuppressWarnings("unchecked")
	public static <T> T parseToNative(Class<T> type, Object value) throws Exception {
		try{
			if(value == null){					
				value = getDefaultValue(type);
				if(value != null)
					return (T) value;
				return null;
			}
			if(type != String.class && String.valueOf(value).equals(""))
				return null;
			
			if(	isNumericType(type) && (value.getClass() == boolean.class || value.getClass() == Boolean.class) ){ 
				// transforma valor boolean a numerico
				return parseToNative(type, ((boolean) value?1:0));
			}else if( type == boolean.class || type == Boolean.class){
				// transforma valor numerico a boolean
				if(isNumericType(value.getClass()))
					return (T)(Double.parseDouble(String.valueOf(value)) == 1? Boolean.TRUE : Boolean.FALSE );
				// transforma valor texto a boolean
				return (T) ((Boolean)Boolean.parseBoolean(String.valueOf(value)));
			}else if( type == DateTime.class){
				if(value.getClass() == java.sql.Date.class){
					DateTime v = new DateTime(((java.sql.Date)value).getTime());
					v.setFormat(DateFormat.YYYY_MM_DD);
					return (T)v ;
				}else if(value.getClass() == java.sql.Timestamp.class){
						DateTime v = new DateTime(((java.sql.Timestamp)value).getTime());
						v.setFormat(DateFormat.YYYY_MM_DD_hh_mm_ss);
						return (T)v ;
				}else if(value.getClass() == java.util.Date.class){
					DateTime v = new DateTime(((java.util.Date)value).getTime());
					v.setFormat(DateFormat.YYYY_MM_DD);
					return (T)v ;
				}
				String val = String.valueOf(value);
				val = val.replace("T", " ");
				DateFormat f = DateTime.inferFormat(val);
				return (T) DateTime.getCustom(val,f != null ? f :DateFormat.YYYY_MM_DD_hh_mm_ss);
			}else if( type == java.util.Date.class){
				if(value.getClass() == String.class){
					DateFormat f = DateTime.inferFormat((String)value);
					f = f == DateFormat.DD_MM_YYYY_hh_mm_ss?DateFormat.DD_MM_YYYY:f;
					f = f == DateFormat.YYYY_MM_DD_hh_mm_ss?DateFormat.YYYY_MM_DD:f;
					DateTime v = DateTime.getCustom((String) value,f != null ? f :DateFormat.YYYY_MM_DD);
					return (T)v.getUtilDate();
				}else if( value.getClass() == DateTime.class){
					return (T)((DateTime)value).getUtilDate();
				}
			}else if( type == java.sql.Date.class){
				if(value.getClass() == String.class){
					DateFormat f = DateTime.inferFormat((String)value);
					f = f == DateFormat.DD_MM_YYYY_hh_mm_ss?DateFormat.DD_MM_YYYY:f;
					f = f == DateFormat.YYYY_MM_DD_hh_mm_ss?DateFormat.YYYY_MM_DD:f;
					DateTime v = DateTime.getCustom((String) value,f != null ? f :DateFormat.YYYY_MM_DD);
					return (T)v.getSqlDate();
				}else if( value.getClass() == DateTime.class){
					return (T)((DateTime)value).getSqlDate();
				}
			}else if( type == java.sql.Timestamp.class){
				if(value.getClass() == String.class){
					DateFormat f = DateTime.inferFormat((String)value);
					DateTime v = DateTime.getCustom((String) value,f != null ? f :DateFormat.YYYY_MM_DD_hh_mm_ss);
					return (T)v.getSqlTimestamp();
				}else if( value.getClass() == DateTime.class){
					return (T)((DateTime)value).getSqlTimestamp();
				}
			}else if( type == java.sql.Time.class){
				return (T) java.sql.Time.valueOf((String) value);
			}else if(type == byte.class	   || type == Byte.class){		return (T) ((Byte)Byte.parseByte(String.valueOf(value)));
			}else if(type == char.class	   || type == Character.class){	return (T) (Character)(String.valueOf(value).charAt(0));
			}else if(type == double.class  || type == Double.class){	return (T) ((Double)Double.parseDouble(String.valueOf(value)));
			}else if(type == int.class	   || type == Integer.class){	return (T) ((Integer)(int)Double.parseDouble(String.valueOf(value)));
			}else if(type == float.class   || type == Float.class){		return (T) ((Float)Float.parseFloat(String.valueOf(value)));
			}else if(type == long.class	   || type == Long.class){		return (T) ((Long)Long.parseLong(String.valueOf(value).trim()));
			}else if(type == short.class   || type == Short.class){		return (T) ((Short)Short.parseShort(String.valueOf(value)));
			}else if(type == BigDecimal.class){		return (T)BigDecimal.valueOf(Double.parseDouble(String.valueOf(value)));
			}else if(type == BigInteger.class){		return (T)BigInteger.valueOf(Long.parseLong(String.valueOf(value)));
			}else if(type == String.class){		
				return (T) String.valueOf(value);
			}
		}catch(Throwable e){
			throw new IllegalArgumentException(String.format("[UtilReflection] Error parsing %s to %s ",value.getClass().getName(),type.getName()),e);
		}
		
		return (T) value;
	}

	public static <T> HashMap<Object,T> parseListToMap(String fieldNameKey,  List<T> list){
		HashMap<Object,T> map = new HashMap<Object,T>();
		for(T obj:list){
			Object k = null;
			try {
				k = getFieldValue(fieldNameKey, obj);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			map.put(k, obj);
		}
		return map;
	}

	public static void setFieldValue(String fieldName,Object obj,Object value) throws Exception{
		Field field = getField(fieldName, obj.getClass());
		setFieldValue(field, obj, value);
	}
	public static void setFieldValue(Field field, Object obj,Object value) throws Exception{
		if(value != null) {
			if(field.isAnnotationPresent(JSONColumn.class) && String.class.isAssignableFrom(value.getClass())) {
				Class<?> type = field.getType();
				if(Collection.class.isAssignableFrom(type)){
					Type parameterType = UtilReflection.getParameterTypes(field)[0];
					List<?> 	list = JSONUtil.decodeList((String)value , parameterType);
					UtilReflection.setFieldValue(field, obj, list);
				}/*else if(Map.class.isAssignableFrom(type)){
					ParameterizedType pt = (ParameterizedType) field.getGenericType();
					Class<?>[] cls = UtilReflection.getParameterClass(pt);
					Class<?> keyType = cls[0];
					Class<?> valueType = cls[1];
					
					if( JSONObject.class.isAssignableFrom(value.getClass()) ){
						//UtilReflection.setFieldValue(field, instance, decodeHash((JSONObject)value, keyType, valueType));
					}else {
						//UtilReflection.setFieldValue(field, instance, decodeHash((JSONArray)value, keyType, valueType));
					}
					//return encodeJsonMap((Map<?, ?>)object);
				}else if(Enum.class.isAssignableFrom(type)){
					//return createJSONObject(object);
				}*/else{
					value = JSONUtil.decodeObject((String)value, type);
					UtilReflection.setFieldValue(field, obj, value);
				}
			}else if(field.getType() != value.getClass() && !field.getType().isAssignableFrom(value.getClass())){
				value = parseToNative(field.getType(),value);
			}
		}
		if(value == null){
			value = getDefaultValue(field.getType());
		}
		String methodName = "set"+field.getName().substring(0, 1).toUpperCase()+field.getName().substring(1);
		Object[] params = new Object[]{value};
		for(Method method:obj.getClass().getMethods()){
			if(method.getName().equals(methodName) && methodParamsMatch(method,params)){
				method.invoke(obj, value);
				return;
			}
		}
    }

	public static Object getDefaultValue(Class<?> type) {
		if(type.isPrimitive()){
			if(type == boolean.class)
				return false;
			if(type == char.class)
				return "";
			if(type == byte.class || type == short.class || type == int.class || type == long.class || type == float.class || type == double.class)
				return 0;
		}
		return null;
	}

	public static boolean containsField(String fieldName, Class<?> clss) {
		try {
			getField(fieldName, clss);
		}catch (Throwable e) {
			return false;
		}
		return true;
	}
	public static Field getField(String fieldName,Class<?> clss) throws NoSuchFieldException, SecurityException{
		Field field = null;
		try {
			field = clss.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			clss = clss.getSuperclass();
			if(clss != null)
				return getField(fieldName, clss);
		}
		if(field == null)
			throw new NoSuchFieldException(String.format("field %s does not exists", fieldName));
		
		return field;
	}
	public static Field getField(String fieldName,Object obj) throws NoSuchFieldException, SecurityException{
		return getField(fieldName, obj.getClass());
	}
	public static List<Method> getMethodsByName(Class<?> clss,String methodName){
		List<Method> methods = new ArrayList<Method>();
		for(Method method:clss.getMethods()){
			if(method.getName().equals(methodName)){
				methods.add(method);
			}
		}
		return methods;
	}
	
	
	/**
	 * retorna el nombre de la ultima clase en el stack
	 * @return
	 */
	public static String  getCallerClassName() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return trace[3].getClassName();
    }
	/**
	 * retorna el nombre simple de la ultima clase en el stack
	 * @return
	 */
	public static String  getSimpleCallerClassName() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String className = trace[3].getClassName();
        return className.substring(className.lastIndexOf(".")+1);
    }
	/**
	 * retorna el nombre método llamado de la ultima clase en el stack
	 * @return
	 */
	public static String  getCallerMethodName() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return trace[3].getMethodName();
    }
	/**
	 * retorna el linea en el método llamado de la ultima clase en el stack
	 * @return
	 */
	public static int  getCallerMethodLine() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return trace[3].getLineNumber();
    }
	
	
	public static String  getSimpleCallerClassName(int callStackDepth) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if(callStackDepth < 0 || callStackDepth >= trace.length)
			callStackDepth = trace.length -1;
		String className = trace[callStackDepth].getClassName();
        return className.substring(className.lastIndexOf(".")+1);
    }
	public static String  getCallerMethodName(int callStackDepth) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if(callStackDepth < 0 || callStackDepth >= trace.length)
			callStackDepth = trace.length -1;
        return trace[callStackDepth].getMethodName();
    }
	public static int  getCallerMethodLine(int callStackDepth) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if(callStackDepth < 0 || callStackDepth >= trace.length)
			callStackDepth = trace.length -1;
		return trace[callStackDepth].getLineNumber();
    }
	public static void  printStackTrace() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(int i=trace.length-1;i>=0;i--)
			System.out.println("trace["+i+"] "+trace[i].getClassName()+"."+trace[i].getMethodName());
			
    }
	/**
	 * 
	 */
	public static List<Class<?>> listAnnotadedClass(Class<? extends Annotation> annotationClass,String[] paths){
		List<Class<?>> annotated = new ArrayList<Class<?>>();
		List<String> files; 

		String classname;
		for(int i=0;i < paths.length;i++){
			files = FileUtil.find(new String[]{paths[i]}, CLASS_EXTENSION);
			for (String f : files) {
				//System.out.println("class found:"+f);
				    try{
				    	if(f.startsWith("jar:")){
				    		classname = f.substring("jar:".length(), f.indexOf(CLASS_EXTENSION)).replaceAll("/", ".");
				    	}
				    	else if(f.indexOf(BASE_URI) >= 0){
				    		classname = f.substring(f.indexOf(BASE_URI)+BASE_URI.length(), f.indexOf(CLASS_EXTENSION)).replaceAll(FILE_SEPARATOR, ".");
				    	}else{
				    		classname = f.substring(paths[i].length()+1,f.indexOf(CLASS_EXTENSION)).replaceAll(FILE_SEPARATOR, ".");
				    	}
				    }catch(Throwable e){
				    	System.out.println(String.format("Error parsing %s",f));
				    	continue;
				    }
					try {
						//System.out.println("[ReflectionUtil] getting class:"+classname);
						Class<?> clss = Class.forName(classname);
						if(clss.isAnnotationPresent(annotationClass)){
							annotated.add(clss);
						}
					}catch (Throwable e) {
						while(e.getCause() != null)
							e = e.getCause();
						//System.err.println("[ReflectionUtil] Error getting class: "+classname+" cause: "+e.getClass().getName()+" "+e.getLocalizedMessage());
					}
					
			}
		}
		
		return annotated;
	}
	/**
	 * Returns all type's methods that has been annotated with specific annotationClass
	 * @param type
	 * @param annotationClass
	 * @return
	 */
	public static List<Method> getAllAnnotatedMethods(Class<?> type,Class<? extends Annotation> annotationClass) {
		List<Method> annotated = new ArrayList<Method>();
		Method[] methods = type.getDeclaredMethods();
		for(Method m:methods){
			if(m.isAnnotationPresent(annotationClass)){
				annotated.add(m);
			}
		}
		return annotated;
	}
	public static List<Method> getAllAnnotatedMethodsReturnType(Class<?> declaredClass,Class<?> returnType,Class<? extends Annotation> annotationClass) {
		List<Method> annotated = new ArrayList<Method>();
		Method[] methods = declaredClass.getDeclaredMethods();
		for(Method m:methods){
			if(m.isAnnotationPresent(annotationClass) && m.getReturnType().isAssignableFrom(returnType)){
				annotated.add(m);
			}
		}
		return annotated;
	}
	public static List<Method> getAllAnnotatedMethods(Class<?> type,Class<? extends Annotation> annotationClass,Object[] paramsValues) {
		List<Method> annotated = new ArrayList<Method>();
		Method[] methods = type.getDeclaredMethods();
		for(Method m:methods){
			if(m.isAnnotationPresent(annotationClass)){
				if(methodParamsMatch(m,paramsValues))
					annotated.add(m);
			}
		}
		return annotated;
	}
	public static boolean methodParamsMatch(Method m, Object[] paramsValues) {
		Class<?>[] paramsTypes = m.getParameterTypes();
		if(paramsTypes.length == paramsValues.length){
			for(int i=0;i<paramsTypes.length;i++){
				if(paramsValues[i] != null){
					if(paramsTypes[i].isPrimitive() && getTypesByWrapper(paramsValues[i].getClass()) != paramsTypes[i])
						return false;
					if(!paramsTypes[i].isPrimitive() && !paramsTypes[i].isAssignableFrom(paramsValues[i].getClass()))
						return false;
				}else if(paramsTypes[i].isPrimitive())
					return false;
			}
		}else
			return false;
		return true;
	}
	public static void methodParamsValidate(Method m, Object[] paramsValues) {
		Class<?>[] paramsTypes = m.getParameterTypes();
		if(paramsTypes.length == paramsValues.length){
			for(int i=0;i<paramsTypes.length;i++){
				if(paramsValues[i] != null){
					if(paramsTypes[i].isPrimitive() && getTypesByWrapper(paramsValues[i].getClass()) != paramsTypes[i])
						throw new IllegalArgumentException(String.format("Error at method %s in the %s:nth parameter.Parameter type '%s' not support '%s' value",m.getName(),i+1,paramsTypes[i].getName(),paramsValues[i].getClass().getName()));
					if(!paramsTypes[i].isPrimitive() && !paramsTypes[i].isAssignableFrom(paramsValues[i].getClass()))
						throw new IllegalArgumentException(String.format("Error at method %s in the %s:nth parameter.Parameter type '%s' not support '%s' value",m.getName(),i+1,paramsTypes[i].getName(),paramsValues[i].getClass().getName()));
				}else if(paramsTypes[i].isPrimitive())
					throw new IllegalArgumentException(String.format("Error at method %s in the %s:nth parameter.Parameter type '%s' not support 'null' value",m.getName(),i+1,paramsTypes[i].getName()));
			}
		}else
			throw new IllegalArgumentException(String.format("Error at method %s .Expected %s parameters, but received %s",m.getName(),paramsTypes.length,paramsValues.length));
	}
	/**
	 * Returns all type's fields that has been annotated with specific annotationClass
	 * @param type
	 * @param annotationClass
	 * @return
	 */
	public static List<Field> getAllAnnotatedFields(Class<?> type,Class<? extends Annotation> annotationClass) {
		List<Field> fields = getAllFields(type);
		List<Field> annotatedFields = new ArrayList<Field>();
		for(Field f: fields){
			if(f.isAnnotationPresent(annotationClass)){
				annotatedFields.add(f);
			}
		}
		return annotatedFields;
	}

	public static boolean isInstanceable(Type type){
		return isInstanceable(type.toString().substring(0, type.toString().indexOf('@')));
	}
	public static boolean isInstanceable(String className){
		try {
			return isInstanceable(Class.forName(className));
		} catch (ClassNotFoundException e) {}
		
		return false;
	}
	public static boolean isInstanceable(Class<?> type){
		try {
			type.newInstance();
			return true;
		} catch (Throwable e) {}
		
		return false;
	}
	/**
	 * Creates a new instance of the type from his type name. 
	 * The class is instantiated as if by a new expression with an empty argument list
	 * 
	 * @param Object
	 * @return Instance of class represented by type name, if the type is not instanceable, returns null
	 */
	public static Object newInstance(Type type){
		try {
			return Class.forName(type.toString().substring(0, type.toString().indexOf('@'))).newInstance();
		} catch (Throwable e) {}
		return null;
	}
	
	public static boolean isParameterizable(Class<?> clss){
		for(Type types :clss.getGenericInterfaces()){
			if(types instanceof ParameterizedType){
				 return true;
			}
		}
		return false;
	}

	public static Type[] getParameterTypes(Field field){
		Type type = field.getGenericType();
		return getParameterTypes(type);
	}

	public static Class<?>[] getParameterClass(Type genericType) throws ClassNotFoundException{
		Type[] tps = getParameterTypes(genericType);
		Class<?>[] cls = new Class[2];

		cls[0] = Class.forName(tps[0].toString().replaceAll("(class[^\\w+\\.?]+)", ""));
		cls[1] = Class.forName(tps[1].toString().replaceAll("(class[^\\w+\\.?]+)", ""));
		
		return cls;
	}		
	/**
	 * Gets all type arguments of a ParameterizedType
	 * @param genericType
	 * @return Type[] if ParameterizedType has arguments, null in otherwise
	 * @throws RuntimeException if type is not a ParameterizedType
	 */
	public static Type[] getParameterTypes(Type genericType){
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] argTypes = paramType.getActualTypeArguments();
            if (argTypes.length > 0) {
                return argTypes;
            }
        }else 
        	throw new RuntimeException(String.format("The tyepe: %s is not ParameterizedType", genericType));
        return null;
	}
	public static boolean isNumericType(Class<?> valueType) {
		return NUMERIC_TYPES.contains(valueType);
	}
	
	
	
	/**
	 * Find recursive al declared an inherits fields
	 * @param fields
	 * @param clss
	 */
	private static void findAllFields(List<Field> fields,HashSet<String> fieldNames, Class<?> clss) {
		if(fieldNames != null){
			for(Field field:clss.getDeclaredFields()){					
				if(!fieldNames.contains(field.getName())){// add only non re-writed fields
					fieldNames.add(field.getName());
					fields.add(field);
				}//else
				//	System.out.println(field.getName()+" not added");
			}
		}else{
			fields.addAll(Arrays.asList(clss.getDeclaredFields()));
		}
		clss = clss.getSuperclass();
		if(clss != null)
			findAllFields(fields,fieldNames, clss);
	}
	public static List<Field> getAllFields(Class<?> clss) {
		return getAllFields(clss, false);
	}
	public static List<Field> getAllFields(Class<?> clss,boolean excludeOverrided) {
		List<Field> fields = new ArrayList<Field>();
		if(Enum.class.isAssignableFrom(clss)){
			for(Field f:clss.getDeclaredFields()){
				if(!f.isEnumConstant() && !(f.getType().isArray() && f.getType().getComponentType() == clss))
					fields.add(f);
			}
			//System.out.println(Arrays.toString(fields.toArray()));
			return fields;
		}
		HashSet<String> fieldNames = null;
		if(excludeOverrided)
			fieldNames	= new HashSet<String>();
		findAllFields(fields,fieldNames, clss);
		return fields;
	}
	public static <T> String resumeFieldsDifferens(T old, T current) {
		List<Object> checked = new ArrayList<Object>();
		String diff = resumeFieldsDifferens(old, current,0,checked);
		System.out.println(diff);
		return diff;
	}
	private static <T> String resumeFieldsDifferens(T old, T current,int deep,List<Object> checked) {
		
		StringBuilder srt = new StringBuilder();
		if(old == null && current != null){
			appendTabs(srt,deep);
			srt.append("null --> ").append(current);
		}else if(current == null && old != null){
			appendTabs(srt,deep);
			srt.append(old).append(" --> null");
		}else if(isNativeType(old.getClass())){
			if(!old.equals(current)){
				appendTabs(srt,deep);
				srt.append(old).append(" --> ").append(current);
			}
		}else{
			if(checked.contains(old) || checked.contains(current))
				return "";//avoid circular structures
			checked.add(old);
			checked.add(current);
			
			List<Field> fields = getAllFields(old.getClass());
			Object oldValue = null;
			Object currentValue = null;
			String differens;
			for(Field f:fields){
				try {
					oldValue = getFieldValue(f, old);
					currentValue = getFieldValue(f, current);
				} catch (Throwable e) {
					continue;
				}
				if(isNativeType(f.getType())){
					if( oldValue != null && !oldValue.equals(currentValue)){
						appendTabs(srt,deep);
						srt.append("[").append(f.getName()).append(": ").append(oldValue).append(" --> ").append(currentValue).append("]\n");
					}else if( currentValue != null && !currentValue.equals(oldValue)){
						appendTabs(srt,deep);
						srt.append("[").append(f.getName()).append(": ").append(oldValue).append(" --> ").append(currentValue).append("]\n");
					}
				}else{
					differens = resumeFieldsDifferens(oldValue, currentValue,deep + 1,checked);
					if(!"".equals(differens.replaceAll("\t", ""))){
						appendTabs(srt,deep);
						srt.append("[").append(f.getName()).append(": {\n").append(differens).append("}]\n");
					}
				}
			}
		}
		return srt.toString();
	}
	private static void appendTabs(StringBuilder srt, int deep) {
		for(int i=0;i<deep;i++)
			srt.append("\t");
	}
	/**
	 * 
	 * @param obj1
	 * @param obj1
	 */
	public static void copy(Object obj1, Object obj2) {
		if(obj1 == null || obj2 == null){
			return;
		}
		
		List<Field> fieldsObj2 = getAllFields(obj2.getClass());
		for(Field field2:fieldsObj2){
			if( field2.getName().equals("serialVersionUID"))
				continue;
			Field field;
			try {
				field = getField(field2.getName(), obj1.getClass());
			}catch (Throwable e1) {
				continue;
			}
			try {
				if(isNativeType(field.getType()))
					setFieldValue(field, obj1, getFieldValue(field, obj2));
				else if(List.class.isAssignableFrom(field.getType())){
					@SuppressWarnings("unchecked")
					List<Object> listOrig = (List<Object>)getFieldValue(field, obj2);
					List<Object> list = new ArrayList<Object>();
					Object vc;
					for(Object v:listOrig){
						if(v != null){
							vc = v.getClass().newInstance();
							copy(vc, v);
							list.add(vc);
						}else
							list.add(null);
					}
					setFieldValue(field, obj1, list);
				}else{// if field is object
					Object fieldCopyInstance = null;
					Object fieldValue = getFieldValue(field, obj2);
					if(fieldValue != null){
						fieldCopyInstance = field.getType().newInstance();
						copy(fieldCopyInstance,fieldValue);
					}
					setFieldValue(field, obj1, fieldCopyInstance);
				}
			} catch (Throwable e) {
				System.err.println(String.format("[UtilReflection.copy] could not copy %s.%s in %s.%s", obj2.getClass().getName(),field2.getName(),obj1.getClass().getName(),field2.getName()));
			}
		}
	}
	/**
	 * 
	 * @param e
	 * @return
	 */
	public static Throwable getRootCause(Throwable e) {
		if(e != null){
			Throwable cause = e;
			while(cause.getCause() != null){
				cause = cause.getCause();
			}
			return cause;
		}
		return null;
	}
	public static Object callMethod(Object instance, String methodName, Object ... params) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(instance != null && methodName != null){
			List<Method> methods = getMethodsByName(instance.getClass(), methodName);
			for(Method m:methods){
				if(methodParamsMatch(m, params)) {
					return m.invoke(instance, params);
				}
			}
		}
		return null;
	}
	public static <T> T[] createArray(Class<T> componentType,int length) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(componentType, length);
		return array ;
	}
	@SuppressWarnings("unchecked")
	public static <T> T[] convertToPrimitiveArray(List<?> list, Class<T> clss) {
		if(clss.isPrimitive()) {
			int length = list.size();
			T[] array = createArray(clss, length);
			for(int i=0;i<length;i++) {
				array[0] = (T) list.get(i);
			}
			return array;
		}
		return null;
	}
	public static Object[] convertToArray(Object object) {
		Object[] array = null;
		Class<?> objCls = object.getClass();
		if(objCls.isArray()) {
			Class<?> cls = objCls.getComponentType();
			if(cls.isPrimitive()) {
				if(boolean.class.isAssignableFrom(cls)) {
					boolean[] ar1 = (boolean[]) object;
					array = new Boolean[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(byte.class.isAssignableFrom(cls)) {
					byte[] ar1 = (byte[]) object;
					array = new Byte[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(char.class.isAssignableFrom(cls)) {
					char[] ar1 = (char[]) object;
					array = new Character[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(short.class.isAssignableFrom(cls)) {
					short[] ar1 = (short[]) object;
					array = new Short[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(int.class.isAssignableFrom(cls)) {
					int[] ar1 = (int[]) object;
					array = new Integer[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(long.class.isAssignableFrom(cls)) {
					long[] ar1 = (long[]) object;
					array = new Long[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(float.class.isAssignableFrom(cls)) {
					float[] ar1 = (float[]) object;
					array = new Float[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}else if(double.class.isAssignableFrom(cls)) {
					double[] ar1 = (double[]) object;
					array = new Double[ar1.length];
					for(int i=0; i< ar1.length;i++) {
						array[i] = ar1[i];
					}
				}
				
			}else {
				array = (Object[])object;
			}
		}
		return array;
	}	
	public static boolean isBooelanType(Class<?> clss) {			
		return clss == boolean.class || clss == Boolean.class;
	}
	/**
	 * Resolve a class real path
	 * @param clss
	 * @return
	 */
	public static String getRealPath(Class<?> clss) {
		ClassLoader loader = clss.getClassLoader();
		String relativePath = clss.getName().replaceAll("\\.", "/");
		return loader.getResource(relativePath+".class").toString();
	}
	/**
	 * Allow retrieve annotation instance values
	 * @param instance
	 * @param methodName
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static <T extends Annotation> Object getAnnotationValue(T instance,String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method[] methods = instance.getClass().getDeclaredMethods();
		for(Method m: methods){
			if(m.getName().equals(methodName)){
				return m.invoke(instance);
			}
		}
		return null;
	}
}
