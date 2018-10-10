package beauty.requestable.util.string;
/**
 * 
 * @author aescobar
 *
 */
public class StringBuilderUtil {

	
	/**
	* Utility method that replace all the occurrences of 'toReplace' by string 'replacement' of StringBuilder.
	* @param strb          the StringBuilder object.
	* @param toReplace   the String that should be replaced.
	* @param replacement the String that has to be replaced by.
	* 
	*/
	public static void replace(StringBuilder strb, String toReplace, String replacement) {      
	    int index = 0;
	    while ((index = strb.indexOf(toReplace,index)) != -1) {
	    	strb.replace(index, index + toReplace.length(), replacement);
	    	index = index + replacement.length();
	    }
	}
	/**
	* Utility method that replace the last occurrence of 'toReplace' by string 'replacement' of StringBuilder.
	* @param strb          the StringBuilder object.
	* @param toReplace   the String that should be replaced.
	* @param replacement the String that has to be replaced by.
	* 
	*/
	public static void replaceLast(StringBuilder strb, String toReplace, String replacement) {      
	    int index = strb.lastIndexOf(toReplace);
	    if(index != -1)
	    	strb.replace(index, index + toReplace.length(), replacement);	    
	}

}
