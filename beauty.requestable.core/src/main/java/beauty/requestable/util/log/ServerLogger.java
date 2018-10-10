package beauty.requestable.util.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import beauty.requestable.util.catalina.CatalinaUtil;
import beauty.requestable.util.date.DateFormat;
import beauty.requestable.util.date.DateTime;
import beauty.requestable.util.reflection.UtilReflection;

public class ServerLogger {
	
	public final static String FILE_ID_COMMON_LOG ="COMMON_LOG";

	private static String LOG_PATH ="/opt/Sphinx/MODULO";
	private static Logger log;
	private static Properties props = null;
	private static PrintStream sysout = null;
	private static PrintStream syserr = null;
	
	static{
		System.out.println("[ServerLogger] Inicializando logger");
		log = Logger.getLogger(ServerLogger.class);
		File logPath = new File(LOG_PATH);
		if(!logPath.exists()){
			logPath.mkdirs();
		}
		try {
			System.out.println("[ServerLogger] to see logs go to "+(new File(LOG_PATH)).getCanonicalPath());
		} catch (IOException e1) {
			System.out.println("[ServerLogger] to see logs go to "+(new File(LOG_PATH)).getAbsolutePath());
		}
		
		props = new Properties();
		try {
			props.load(ServerLogger.class.getResourceAsStream("/META-INF/log.properties"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		ServerLogger.sysout = System.out;
		System.setOut(new PrintStream(System.out) {
				            public void print(final String string) {
				            	try {
					            	if(string != null && !string.startsWith("log4j:")){
						            	info(FILE_ID_COMMON_LOG, string,	UtilReflection.getSimpleCallerClassName(4),
																			UtilReflection.getCallerMethodName(4),
																			UtilReflection.getCallerMethodLine(4));
					            	}
				            	} catch (Throwable e) {
				        			e.printStackTrace();
				        		}
				            	sysout.print(string);
				            }
				        }
		);
		ServerLogger.syserr = System.err;
	    System.setErr(new PrintStream(System.err) {
				            public void print(final String string) {
				            	try {
					            	if(string != null && !string.startsWith("log4j:")){
						            	error(FILE_ID_COMMON_LOG, string,	UtilReflection.getSimpleCallerClassName(-1),
																			UtilReflection.getCallerMethodName(-1),
																			UtilReflection.getCallerMethodLine(-1));
					            	}
					            } catch (Throwable e) {
				        			e.printStackTrace();
				        		}
				            	syserr.print(string);
				            }
				        }
	     );
	    
	}

	private ServerLogger() {
	}

	private static void checkLog4jConfiguration(String fileId) {
		DateTime d = DateTime.getNow();
		int mm = d.getMonth()+1;
		String logPath = String.format("%s/%s/%s/%s/",LOG_PATH,fileId,d.getYear(),mm<10?"0"+mm:mm);
		String fileName = String.format("%s%s-%s.log",logPath,CatalinaUtil.getServerName(),d.toString(DateFormat.YYYY_MM_DD));
		if(!fileName.equals(props.getProperty("log4j.appender.FILE.File"))){
		    props.setProperty("log4j.appender.FILE.File", fileName);
		    LogManager.resetConfiguration(); 
		    PropertyConfigurator.configure(props); 		
		}
	}
	private static void info(String fileId,String text,String className,String methodName,int methodLine){
		checkLog4jConfiguration(fileId);
		log.info(String.format(" [%s.%s:%s] %s",className,methodName,methodLine,text));
	}
	private static void error(String fileId,String text,String className,String methodName,int methodLine){
		checkLog4jConfiguration(fileId);
		log.error(String.format(" [%s.%s:%s] %s",className,methodName,methodLine,text));
	}
	
	
	/**
	 * Imprime un log INFO<br/>
	 * EL valor dado por <strong>fileId</strong> se utiliza para escribir un log especifico en /opt/Sphinx/MODULO/fileId/YYYY/MM/ServerName-YYYY-MM-DD.log
	 * @param fileId Nombre del Log especifico
	 * @param text   Texto del log
	 */
	public static void info(String fileId,String text){
		info(fileId, text,	UtilReflection.getSimpleCallerClassName(),
							UtilReflection.getCallerMethodName(),
							UtilReflection.getCallerMethodLine());
	}
	/**
	 * Imprime un log INFO en COMMON_LOG<br/>
	 * La ruta de este log es  /opt/Sphinx/MODULO/COMMON_LOG/YYYY/MM/ServerName-YYYY-MM-DD.log
	 * @param text   Texto del log
	 */
	public static void info(String text){
		info(FILE_ID_COMMON_LOG, text,	UtilReflection.getSimpleCallerClassName(),
							UtilReflection.getCallerMethodName(),
							UtilReflection.getCallerMethodLine());
	}
	/**
	 * Imprime un log ERROR<br/>
	 * EL valor dado por <strong>fileId</strong> se utiliza para escribir un log especifico en /opt/Sphinx/MODULO/fileId/YYYY/MM/ServerName-YYYY-MM-DD.log
	 * @param fileId Nombre del Log especifico
	 * @param text   Texto del log
	 */
	public static void error(String fileId,String text){
		error(fileId, text, UtilReflection.getSimpleCallerClassName(),
							UtilReflection.getCallerMethodName(),
							UtilReflection.getCallerMethodLine());
	}
	/**
	 * Imprime un log ERROR en COMMON_LOG<br/>
	 * La ruta de este log es  /opt/Sphinx/MODULO/COMMON_LOG/YYYY/MM/ServerName-YYYY-MM-DD.log
	 * @param text   Texto del log
	 */
	public static void error(String text){
		error(FILE_ID_COMMON_LOG, text,	UtilReflection.getSimpleCallerClassName(),
							UtilReflection.getCallerMethodName(),
							UtilReflection.getCallerMethodLine());
	}	

	public static void main(String[] args) {
		ServerLogger.info("PRECIO_ESPECIAL","test log");
		ServerLogger.info("PRECIO_ESPECIAL2","test log");
		System.out.println("out log");
		try {
			throw new Exception("test exception");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
