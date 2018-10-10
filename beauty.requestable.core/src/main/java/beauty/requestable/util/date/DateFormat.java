package beauty.requestable.util.date;

public enum DateFormat {
	YYYY_MM_DD,
	YYYY_MM_DD_hh_mm_ss,
	DD_MM_YYYY,
	DD_MM_YYYY_hh_mm_ss,
	YYYY_MONTH_NAME_DD,
	YYYY_MONTH_NAME_DD_hh_mm_ss,
	DD_MONTH_NAME_YYYY,
	DD_MONTH_NAME_YYYY_hh_mm_ss,
	E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone;
	
	public String toString() {
		switch (this) {
			case DD_MM_YYYY:
			case YYYY_MM_DD:
				return super.toString().replace("_", "-").replace("D", "d").replace("Y", "y");
			case DD_MM_YYYY_hh_mm_ss:
			case YYYY_MM_DD_hh_mm_ss:
				return super.toString().replace("_", "-").replace("D", "d").replace("Y", "y").substring(0, 10)+" hh:mm:ss";
			case DD_MONTH_NAME_YYYY:
			case YYYY_MONTH_NAME_DD:
				return super.toString().replace("_", " ").replace("D", "d").replace("Y", "y").replace("MONTH NAME", "MMMMM");
				
			case DD_MONTH_NAME_YYYY_hh_mm_ss:				
			case YYYY_MONTH_NAME_DD_hh_mm_ss:
				return super.toString().replace("_", " ").replace("D", "d").replace("Y", "y").replace("MONTH NAME", "MMMMM").substring(0, 13)+" hh:mm:ss";
			case E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone:
				return super.toString().replace("_", " ").replace("D", "d").replace("Y", "y").replace("MONTH NAME", "MMMMM").substring(0, 17)+" HH:mm:ss z";
		}
		return "";
	}
	public static void main(String[] args) {
		for(DateFormat df:DateFormat.values()){
			System.out.println(df);
		}
	}
}