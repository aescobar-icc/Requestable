package beauty.requestable.util.math;

public class UtilMath {

	public static double getRandom(double begin,double end){
		return Math.random()*(end-begin)+begin;
	}
	public static int getRandom(int begin,int end){
		return (int)getRandom((double)begin, (double)end);
	}
	public static boolean isNumericValue(String value){
		if(value == null || value.trim().equals(""))
			return false;
		try{
			Integer.parseInt(value);
			return true;
		}catch(NumberFormatException e){}
		try{
			Double.parseDouble(value);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	public static boolean isInteger(String value){
		if(value == null || value.trim().equals(""))
			return false;
		try{
			Integer.parseInt(value);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	/**
	 * return int value
	 * if the string does not contain a parsable integer, returns 0
	 * @param value
	 * @return
	 */
	public static int parseInt(String value){
		if(value == null || value.trim().equals(""))
			return 0;
		try{
			return Integer.parseInt(value);
		}catch(NumberFormatException e){}
		return 0;
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(getRandom(5, 9));
		}
	}
}
