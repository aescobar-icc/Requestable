package beauty.requestable.util.format;

public class UtilStringFormat {
	/**
	 * convierte el string value de formato camelcase to underscore
	 * @param value
	 * @return
	 */
	public static String camelToUnder(String value){
		String regex = "([a-z])([A-Z])";
		String replacement = "$1_$2";

		return value.replaceAll(regex, replacement).toLowerCase();
		 
	}
	public static String underToCamel(String value){
		StringBuilder strb = new StringBuilder();
		String[] cases = value.toLowerCase().split("_");
		strb.append(cases[0]);
		for(int i=1;i<cases.length;i++){
			strb.append(cases[i].substring(0,1).toUpperCase());
			strb.append(cases[i].substring(1));
		}

		return strb.toString();
		 
	}
}
