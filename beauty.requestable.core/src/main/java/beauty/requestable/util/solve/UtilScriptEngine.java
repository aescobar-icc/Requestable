package beauty.requestable.util.solve;


import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import beauty.requestable.util.date.DateFormat;
import beauty.requestable.util.date.DateTime;
import beauty.requestable.util.serialize.JSONUtil;
/**
 * 
 * @author aescobar
 *
 */
public class UtilScriptEngine {
	private static HashMap<Long, ScriptEngine> context = new HashMap<Long, ScriptEngine>();
	
	private static ScriptEngine getScriptEngine(){
		long id = Thread.currentThread().getId();
		ScriptEngine engine = context.get(id);
		if(engine == null){
			ScriptEngineManager engineManager =  new ScriptEngineManager();
			engine = engineManager.getEngineByName("nashorn");
			context.put(id, engine);
		}
		return engine;
	}
	public static Object declareVar(String name,String value) throws ScriptException{
		String decl = String.format("var %s = %s;", name,value);
		//System.out.println(decl);
		return getScriptEngine().eval(decl);
	}
	public static Object eval(String script) throws ScriptException{		
		Object res = getScriptEngine().eval(script);
		//System.out.println(script);
		return res;
	}
	public static Object eval(String[] varNames,Object[] varValues,String script) throws ScriptException{
		
		if(varNames == null || varNames.length == 0)
			throw new ScriptException("Parameter 'varNames' must contains elements");
		if(varValues == null || varValues.length == 0)
			throw new ScriptException("Parameter 'varValues' must contains elements");
		if(varNames.length != varValues.length)
			throw new ScriptException("Parameters 'varNames' and 'varValues' must contains the same number of elements.");
		
		try {
			Object val;
			for(int i=0; i< varNames.length;i++){
				val = varValues[i];
				if(val == null)
					declareVar(varNames[i], null);
				else if(val.getClass().isAssignableFrom(String.class))
					declareVar(varNames[i],String.format("'%s'",val));
				else
					declareVar(varNames[i], JSONUtil.encodeJsonString(val));
			}
	
			return eval(script);
		} catch (ScriptException e) {
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new ScriptException(String.format("Error evaluating script, detail: %s",e.toString()));
		}
	}
	public static void main(String[] args) {
		try {
			String script = "username.name.xx == 'aescobarx' || varx == 0 || varB == true || varB2 == true || varArr[1].indexOf('x2') == 0 || varDate == '2016-08-05' ";

			String[] params = new String[]{"username","varx","varB","varB2","varArr","varDate"};
			Object[] values = new Object[]{"aescobar",5,false,Boolean.FALSE,new String[]{"x1","x2","x3"},DateTime.getNow(DateFormat.YYYY_MM_DD)};
			System.out.println(eval(params, values, script));
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
