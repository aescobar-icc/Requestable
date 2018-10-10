package beauty.requestable.util.sql.postgres;

import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import beauty.requestable.util.reflection.UtilReflection;


public class Type implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -447819047097975856L;

	private String name;
	private String fieldName;
	private String type;
	private Integer jdbcType; // from java.sql.types
	private Class<?> javaType;
	private Object value = null;
	
	//for composite types
	private List<Type> attributes = null;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.jdbcType = UtilPostgres.getType(type);
		this.javaType = UtilPostgres.getJavaType(type);
		if(isArray() && javaType != null)
			this.type = UtilPostgres.getPostgresType(javaType.getComponentType().getSimpleName());
		else
			this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		if(value != null && javaType != null && javaType != value.getClass() && !javaType.isAssignableFrom(value.getClass()) ){
			try {
				this.value = UtilReflection.parseToNative(javaType, value);
			} catch (Throwable e) {
				this.value = value;
			}
		}else
			this.value = value;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Integer getJdbcType() {
		return jdbcType;
	}
	public Class<?> getJavaType() {
		return javaType;
	}
	public List<Type> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<Type> attributes) {
		this.attributes = attributes;
	}	
	public boolean addAttribute(Type attr){
		if(attr == null)
			return false;
		if(attributes == null)
			attributes = new ArrayList<Type>();
		
		return attributes.add(attr);
			
	}
	public boolean hasAttributes(){
		return attributes != null && attributes.size() > 0;
	}
	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder(
		String.format("Parameter [name=%s, fieldName=%s, type=%s, jdbcType=%s, javaType=%s, value=%s, attributes=",
						name, fieldName, type, jdbcType, javaType, value));
		if(attributes != null){
			strb.append("[");
			for(Type p:attributes){
				strb.append("\n\t\t").append(p).append(",");
			}
			strb.append("\t]\n");
			strb.append("];");
		}else
			strb.append("null];");
			
		return strb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj instanceof String) {
			return this.name.equals(obj);
		}
		return super.equals(obj);
	}
	public boolean isArray() {
		return jdbcType != null && jdbcType == Types.ARRAY;
	}
}
