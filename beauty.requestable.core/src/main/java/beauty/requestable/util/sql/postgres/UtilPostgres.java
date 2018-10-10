package beauty.requestable.util.sql.postgres;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;

public class UtilPostgres {

	private static HashMap<String, Class<?>> mappingJavaTypeNames;
	private static HashMap<String, String> javaPostgres;
	private static HashMap<String, Integer> mappingTypes;
	static{
		mappingJavaTypeNames = new HashMap<String, Class<?>>();
		mappingJavaTypeNames.put("bit",Boolean.class);
		mappingJavaTypeNames.put("bigint",Long.class);
		mappingJavaTypeNames.put("boolean",Boolean.class);
		mappingJavaTypeNames.put("char",String.class);
		mappingJavaTypeNames.put("character",String.class);
		mappingJavaTypeNames.put("character varying",String.class);
		mappingJavaTypeNames.put("date",java.sql.Date.class);
		mappingJavaTypeNames.put("date without time zone",java.sql.Date.class);
		mappingJavaTypeNames.put("decimal",BigDecimal.class);
		mappingJavaTypeNames.put("double precision",Double.class);
		mappingJavaTypeNames.put("float",Double.class);
		mappingJavaTypeNames.put("int",Integer.class);
		mappingJavaTypeNames.put("int2",Integer.class);
		mappingJavaTypeNames.put("int3",Integer.class);
		mappingJavaTypeNames.put("int4",Integer.class);
		mappingJavaTypeNames.put("integer",Integer.class);
		mappingJavaTypeNames.put("longvarchar",String.class);
		mappingJavaTypeNames.put("numeric",BigDecimal.class);
		mappingJavaTypeNames.put("real",Float.class);
		mappingJavaTypeNames.put("smallint",Short.class);
		mappingJavaTypeNames.put("text",String.class);
		mappingJavaTypeNames.put("tinyint",Byte.class);
		mappingJavaTypeNames.put("timestamp without time zone",java.sql.Timestamp.class);
		mappingJavaTypeNames.put("timestamp",java.sql.Timestamp.class);
		mappingJavaTypeNames.put("time without time zone",java.sql.Time.class);
		mappingJavaTypeNames.put("varchar",String.class);
		

		javaPostgres = new HashMap<String, String>();
		javaPostgres.put("boolean","boolean");
		javaPostgres.put("long","bigint");
		javaPostgres.put("string","varchar");
		javaPostgres.put("date","date");
		javaPostgres.put("bigdecimal","numeric");
		javaPostgres.put("double","double precision");
		javaPostgres.put("integer","integer");
		javaPostgres.put("float","real");
		javaPostgres.put("short","smallint");
		javaPostgres.put("byte","tinyint");
		javaPostgres.put("timestamp","timestamp");
		

		mappingTypes = new HashMap<String, Integer>();

		mappingTypes.put("ARRAY",		Types.ARRAY);
		mappingTypes.put("BIGINT",		Types.BIGINT);
		mappingTypes.put("BINARY",		Types.BINARY);
		mappingTypes.put("BIT",			Types.BIT);
		mappingTypes.put("BLOB",		Types.BLOB);
		mappingTypes.put("BOOLEAN",		Types.BOOLEAN);
		mappingTypes.put("CHAR",		Types.CHAR);
		mappingTypes.put("CHARACTER",	Types.CHAR);
		mappingTypes.put("CLOB",		Types.CLOB);
		mappingTypes.put("DATALINK",	Types.DATALINK);
		mappingTypes.put("DATE",		Types.DATE);
		mappingTypes.put("DECIMAL",		Types.DECIMAL);
		mappingTypes.put("DISTINCT",	Types.DISTINCT);
		mappingTypes.put("DOUBLE",		Types.DOUBLE);
		mappingTypes.put("DOUBLE PRECISION",Types.DOUBLE);
		mappingTypes.put("FLOAT",		Types.FLOAT);
		mappingTypes.put("INTEGER",		Types.INTEGER);
		mappingTypes.put("INT",			Types.INTEGER);
		mappingTypes.put("INT2",		Types.INTEGER);
		mappingTypes.put("INT3",		Types.INTEGER);
		mappingTypes.put("INT4",		Types.INTEGER);
		mappingTypes.put("JAVA_OBJECT",Types.JAVA_OBJECT);
		mappingTypes.put("LONGNVARCHAR",Types.LONGNVARCHAR);
		mappingTypes.put("LONGVARBINARY",Types.LONGVARBINARY);
		mappingTypes.put("LONGVARCHAR",Types.LONGVARCHAR);
		mappingTypes.put("NCHAR",Types.NCHAR);
		mappingTypes.put("NCLOB",Types.NCLOB);
		mappingTypes.put("NULL",Types.NULL);
		mappingTypes.put("NUMERIC",Types.NUMERIC);
		mappingTypes.put("NVARCHAR",Types.NVARCHAR);
		mappingTypes.put("OTHER",Types.OTHER);
		mappingTypes.put("REAL",Types.REAL);
		mappingTypes.put("REF",Types.REF);
		mappingTypes.put("REF_CURSOR",Types.REF_CURSOR);
		mappingTypes.put("ROWID",Types.ROWID);
		mappingTypes.put("SMALLINT",Types.SMALLINT);
		mappingTypes.put("SQLXML",Types.SQLXML);
		mappingTypes.put("STRUCT",Types.STRUCT);
		mappingTypes.put("TIME",					Types.TIME);
		mappingTypes.put("TIME WITHOUT TIME ZONE",	Types.TIME);
		mappingTypes.put("TIME WITH TIMEZONE",Types.TIME_WITH_TIMEZONE);
		mappingTypes.put("TIMESTAMP",					Types.TIMESTAMP);
		mappingTypes.put("TIMESTAMP WITHOUT TIME ZONE",	Types.TIMESTAMP);
		mappingTypes.put("TIMESTAMP WITH TIMEZONE",	Types.TIMESTAMP_WITH_TIMEZONE);
		mappingTypes.put("TINYINT",Types.TINYINT);
		mappingTypes.put("VARBINARY",Types.VARBINARY);
		mappingTypes.put("VARCHAR",				Types.VARCHAR);
		mappingTypes.put("CHARACTER VARYING",	Types.VARCHAR);
		mappingTypes.put("TEXT",				Types.VARCHAR);
		
	}
	public static Class<?> getJavaType(String postgresType){
		boolean isArray = postgresType.endsWith("[]");
		if(isArray) {
			postgresType = postgresType.replace("[]", "");
		}
		Class<?> type = mappingJavaTypeNames.get(postgresType.toLowerCase());
		if(type != null && isArray) {
			return Array.newInstance(type, 0).getClass();
			
		}
		
		//throw new RuntimeException(String.format("[UtilPostgres] postgres type: %s is not mapping as JAVA TYPE yet", postgresType));
		return type;
		
	}
	public static String getPostgresType(String javaType){

		if(javaPostgres.containsKey(javaType.toLowerCase()))
			return javaPostgres.get(javaType.toLowerCase());
		
		System.out.println(String.format("[UtilPostgres] WARNING: JAVA type: %s is not been mapped as POSTGRES TYPE yet", javaType));
		//throw new RuntimeException(String.format("[UtilPostgres] JAVA type: %s is not mapping as POSTGRES TYPE yet", javaType));
		return null;
		
	}

	public static Integer getType(String postgresType){
		boolean isArray = postgresType.endsWith("[]");
		if(isArray) {
			return Types.ARRAY;
		}
		if(mappingTypes.containsKey(postgresType.toUpperCase()))
			return mappingTypes.get(postgresType.toUpperCase());

		//System.out.println(String.format("[UtilPostgres] WARNING: postgres type: %s is not been mapped yet", postgresType));
		//throw new RuntimeException(String.format("[UtilPostgres] postgres type: %s is not mapping yet", postgresType));
		return null;
	}
}
