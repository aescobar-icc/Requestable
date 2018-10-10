package beauty.requestable.util.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FormatoNumero {
	
	static public String formatearPesos(double valor){
		DecimalFormat formato = new DecimalFormat("###,###.##");
	    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    dfs.setDecimalSeparator(',');
	    dfs.setGroupingSeparator('.');
	    formato.setDecimalFormatSymbols(dfs);
	    return formato.format(valor);
	}
	
	static public String formatearDolar(double valor){
		DecimalFormat formato = new DecimalFormat("###,###.##");
	    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    dfs.setDecimalSeparator('.');
	    dfs.setGroupingSeparator(',');
	    formato.setDecimalFormatSymbols(dfs);
	    return formato.format(valor);
	}
	
	/**
	 * @name	: formatNumber (Double)
	 * @author	: ppumarino
	 * @desc	: recieves a number variable (Double) and returns a string with the desired format
	 * @params	: value, prefix, sufix, thousandsSeparator, decimalSeparator, decimalPrecision, ifNull
	 */
	public static String formatNumber(Double value, Integer decimalPrecision, String prefix, String sufix, Character thousandsSeparator, Character decimalSeparator, String ifNull){
		//Check value and ifNull
		if(ifNull == null)
			ifNull = "-";
		if(value == null)
			return ifNull;
		
		//Check thousands and decimal separator, if null use default
		if(thousandsSeparator == null)
			thousandsSeparator = '.';
		if(decimalSeparator == null){
			if(thousandsSeparator != ',')
				decimalSeparator = ',';
			else
				decimalSeparator = '.';
		}
		
		//Check prefix and sufix
		if(prefix == null)
			prefix = "";
		if(sufix == null)
			sufix = "";
		
		//Round number if decimalPrecision is not null and bigger or equals than zero
		if(decimalPrecision != null && decimalPrecision >= 0)
			value = round(value, decimalPrecision);
		
		//Format thousands and decimal separator
		DecimalFormat format = new DecimalFormat("###,###.##");
		DecimalFormatSymbols formatSimbols = new DecimalFormatSymbols();
		formatSimbols.setDecimalSeparator(decimalSeparator);
		formatSimbols.setGroupingSeparator(thousandsSeparator);
		format.setDecimalFormatSymbols(formatSimbols);
		
		return prefix + format.format(value) + sufix;
	}
	
	/**
	 * @name	: formatNumber (without decimal and thousands separators needed) (Double)
	 * @author	: ppumarino
	 * @desc	: recieves a number variable (Double) and returns a string with the desired format. Asumes decimal separator ',' and thousandsSeparator '.'
	 * @params	: value, prefix, sufix, decimalPrecision, ifNull.
	 */
	public static String formatNumber(Double value, Integer decimalPrecision, String prefix, String sufix, String ifNull){ 
		return formatNumber(value, decimalPrecision, prefix, sufix, '.', ',', ifNull);
	}
	
	/**
	 * @name	: formatNumber (Integer)
	 * @author	: ppumarino
	 * @desc	: recieves a number variable (Integer) and returns a string with the desired format
	 * @params	: value, prefix, sufix, thousandsSeparator, ifNull
	 */
	public static String formatNumber(Integer value, String prefix, String sufix, Character thousandsSeparator, String ifNull){
		Double val = null;
		if(value != null)
			val = value.doubleValue();
		
		return formatNumber(val, 0, prefix, sufix, thousandsSeparator, ',', ifNull);
	}
	
	public static double round(double value, int decimalPlaces) {
	    if (decimalPlaces < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
