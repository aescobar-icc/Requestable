package beauty.requestable.util.date;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Esta clase permite manejar la fecha
 * @author aescobar
 *
 */
public class DateTime implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String REGEX_NAMED_FORMAT = "(Mon|Tue|Wed|Thu|Fri|Sat|Sun),\\s+(\\d+)\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d+)\\s+(\\d+):(\\d+):(\\d+)\\s+(\\w+)";
	private static final List<String> MONTHS = Arrays.asList(   "Enero",
																"Febrero",
																"Marzo",
																"Abril",
																"Mayo",
																"Junio",
																"Julio",
																"Agosto",
																"Septiembre",
																"Octubre",
																"Noviembre",
																"Diciembre");
	private static final List<String> MONTHS_EN = Arrays.asList(   "Jan",
																	"Feb",
																	"Mar",
																	"Apr",
																	"May",
																	"Jun",
																	"Jul",
																	"Aug",
																	"Sep",
																	"Oct",
																	"Nov",
																	"Dec");

	private static final List<String> DAYS = Arrays.asList(     "Domingo",
																"Lunes",
																"Martes",
																"Miércoles",
																"Jueves",
																"Viernes",
																"Sabado");

	private static final double MIN_MILI  = 60 * 1000;
	private static final double HOUR_MILI = 60 * MIN_MILI;
	private static final double DATE_MILI = 24 * HOUR_MILI;
	
	private Calendar calendar;
	private DateFormat format;
	private DateFormatSeparator separator;
	/**
	 * Crea una instancia de DateTime con la fecha y hora actual
	 */
	public DateTime(){
		calendar = Calendar.getInstance();
		format = DateFormat.YYYY_MM_DD;
		separator = DateFormatSeparator.HYPHEN;
	}

	/**
	 * Crea una instancia de DateTime - Sets this Calendar's current time from the given long value. 
	 */
	public DateTime(long millis){
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		format = DateFormat.YYYY_MM_DD_hh_mm_ss;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Crea una instancia de DateTime - Sets this Calendar's current time from the given long value. 
	 */
	public DateTime(long millis,TimeZone timeZone){
		calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTimeInMillis(millis);
		format = DateFormat.YYYY_MM_DD_hh_mm_ss;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Crea una instancia de DateTime con la fecha y hora actual
	 */
	public DateTime(Calendar calendar){
		this.calendar = calendar;
		format = DateFormat.YYYY_MM_DD;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Crea una instancia de DateTime con la fecha y hora actual
	 */
	public DateTime(Calendar calendar,DateFormat format){
		this.calendar = calendar;
		this.format = format;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Crea una instancia de DateTime con la fecha y hora actual
	 */
	public DateTime(Calendar calendar,DateFormat format,DateFormatSeparator separator){
		this.calendar = calendar;
		this.format = format;
		this.separator = separator;
	}
	/**
	 * Crea una instancia de DateTime con la fecha y hora actual, especificando el formato deseado
	 */
	public DateTime(DateFormat format){
		calendar = Calendar.getInstance();
		setFormat(format);
	}
	/**
	 * Crea una instancia de DateTime con la fecha y hora actual, especificando el formato y separador de fecha deseado 
	 */
	public DateTime(DateFormat format,DateFormatSeparator separator){
		calendar = Calendar.getInstance();
		this.format = format;
		this.separator = separator;
	}
	/**
	 * Crea una instancia de DateTime con el año mes y dia especificado
	 */
	public DateTime(int year,int month,int date){
		calendar = Calendar.getInstance();
		calendar.set(year, month, date,0,0,0);
		calendar.clear(Calendar.MILLISECOND);
		format = DateFormat.YYYY_MM_DD;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Crea una instancia de DateTime con el año mes y dia especificando el formato deseado
	 */
	public DateTime(int year,int month,int date,DateFormat format){
		calendar = Calendar.getInstance();
		calendar.set(year, month, date,0,0,0);
		calendar.clear(Calendar.MILLISECOND);
		setFormat(format);
	}
	/**
	 * Crea una instancia de DateTime con el año, mes y dia especificando el formato y separador de fecha deseado 
	 */
	public DateTime(int year,int month,int date,DateFormat format,DateFormatSeparator separator){
		calendar = Calendar.getInstance();
		calendar.set(year, month, date,0,0,0);
		calendar.clear(Calendar.MILLISECOND);
		this.format = format;
		this.separator = separator;
	}
	/**
	 * Crea una instancia de DateTime con el año, mes y dia especificado
	 */
	public DateTime(int year,int month,int date,int hour,int min,int sec){
		calendar = Calendar.getInstance();
		calendar.set(year, month, date,hour,min,sec);
		calendar.clear(Calendar.MILLISECOND);
		format = DateFormat.YYYY_MM_DD_hh_mm_ss;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Crea una instancia de DateTime con el año, mes y dia especificando el formato deseado
	 */
	public DateTime(int year,int month,int date,int hour,int min,int sec,DateFormat format){
		calendar = Calendar.getInstance();
		calendar.set(year, month, date,hour,min,sec);
		calendar.clear(Calendar.MILLISECOND);
		setFormat(format);
	}
	/**
	 * Crea una instancia de DateTime con el año, mes y dia especificando el formato y separador de fecha deseado 
	 */
	public DateTime(int year,int month,int date,int hour,int min,int sec,DateFormat format,DateFormatSeparator separator){
		calendar = Calendar.getInstance();
		calendar.set(year, month, date,hour,min,sec);
		calendar.clear(Calendar.MILLISECOND);
		this.format = format;
		this.separator = separator;
	}

	public DateTime(java.util.Date fecha){
		this.calendar = Calendar.getInstance();
		this.calendar.setTime(fecha);
		format = DateFormat.YYYY_MM_DD_hh_mm_ss;
		separator = DateFormatSeparator.HYPHEN;
	}
	
	public DateTime(java.sql.Date fecha) {
		this.calendar = Calendar.getInstance();
		this.calendar.setTime(fecha);
		format = DateFormat.YYYY_MM_DD;
		separator = DateFormatSeparator.HYPHEN;
	}
	public DateTime(java.sql.Timestamp fecha) {
		this.calendar = Calendar.getInstance();
		this.calendar.setTimeInMillis(fecha.getTime());;
		format = DateFormat.YYYY_MM_DD_hh_mm_ss;
		separator = DateFormatSeparator.HYPHEN;
	}
	/**
	 * Retorna un objeto java.util.Date que representa este objeto DateTime
	 * @return
	 */
	public Date getUtilDate(){
	    return calendar.getTime();
	}
	public java.sql.Date getSqlDate(){
		return new java.sql.Date(calendar.getTimeInMillis());
	}
	public java.sql.Timestamp getSqlTimestamp(){
		return new java.sql.Timestamp(calendar.getTimeInMillis());
	}
	public int getDay(){
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public int getMonth(){
		return calendar.get(Calendar.MONTH);
	}
	
	public String getMonthName(){
		return MONTHS.get(this.getMonth());
	}
	
	public int getLastDayOfMonth(){
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	public DateTime getLastDateOfMonth() {
		int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		return new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day);
	}
	public DateTime getFirstDateOfMonth() {
		return new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
	}
	
	public int getYear(){
		return calendar.get(Calendar.YEAR);
	}

	public int getHourOfDay(){
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute(){
		return calendar.get(Calendar.MINUTE);
	}

	public int getSecond(){
		return calendar.get(Calendar.SECOND);
	}	
	
	public int getWeekOfYear(){
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}
	
	public int getISOWeekOfYear(){
		Calendar calendar = (Calendar) this.calendar.clone();
		calendar.setMinimalDaysInFirstWeek(4);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		/* Get ISO8601 week number */
		
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}
	
	public DateTime getStartOfWeek(int firstDayOfWeek){
		Calendar calendar = (Calendar) this.calendar.clone();
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);

		//Aseguro que el primer dia de la semana sea un lunes
		calendar.setFirstDayOfWeek(firstDayOfWeek);
		
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		
		return new DateTime(calendar);
	}

	public int getDayOfTheWeek(){
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public void set(int year, int month, int date, int hourOfDay, int minute, int second){
		calendar.set(year, month, date, hourOfDay, minute,second);
	}
	
	public void setHHMMSS(int hourOfDay, int minute, int second){
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute,second);
		switch (format) {
			case DD_MM_YYYY:
			case DD_MM_YYYY_hh_mm_ss:
				format = DateFormat.DD_MM_YYYY_hh_mm_ss;
				break;
			case YYYY_MM_DD:
			case YYYY_MM_DD_hh_mm_ss:
				format = DateFormat.YYYY_MM_DD_hh_mm_ss;
				break;
			default:
				break;
		}
	}
	
	public Calendar getCalendar(){
		return (Calendar)calendar.clone();
	}
	private DateTime add(int type,int value){
		DateTime d = new DateTime((Calendar)calendar.clone(),format);
		d.calendar.add(type, value);
		//System.out.println("add: "+ d);
		return d;
	}
	
	public DateTime addAllTime(int hours,int mins,int secs){

		DateTime fecha = add(Calendar.HOUR, hours);
		fecha = fecha.add(Calendar.MINUTE, mins);
		fecha = fecha.add(Calendar.SECOND, secs);
		return fecha;
	}
	
	public DateTime addSeconds(int secs){
		return add(Calendar.SECOND, secs);
	}
	public DateTime addMinutes(int mins){
		return add(Calendar.MINUTE, mins);
	}
	public DateTime addHours(int hours){
		return add(Calendar.HOUR, hours);
	}
	public DateTime addDays(int days){
		return add(Calendar.DATE, days);
	}
	public DateTime addMonths(int months){
		return add(Calendar.MONTH, months);
	}
	public DateTime addYears(int years){
		return add(Calendar.YEAR, years);
	}
	private double diff(int type,DateTime other){
		long miliMe    = calendar.getTimeInMillis();
		long miliOther = other.calendar.getTimeInMillis();
		
		long diff = miliMe - miliOther;
		switch (type) {
			case Calendar.SECOND:
				return Math.abs(diff / 1000.0);
			case Calendar.MINUTE:
				return Math.abs(diff / MIN_MILI);
			case Calendar.HOUR:
				return Math.abs(diff / HOUR_MILI);
			case Calendar.DATE:
				return Math.abs(diff / DATE_MILI);
		}
		return 0;
	}

	public double diffSeconds(DateTime other){
		return diff(Calendar.SECOND, other);
	}
	public double diffMinutes(DateTime other){
		return diff(Calendar.MINUTE, other);
	}
	public double diffHours(DateTime other){
		return diff(Calendar.HOUR, other);
	}
	public double diffDays(DateTime other){
		return diff(Calendar.DATE, other);
	}
	public String diffDDHHMMSS(DateTime other){
		long difd = (long) diff(Calendar.DATE, other);
		long difh = (long) diff(Calendar.HOUR, other);
		long difm = (long) diff(Calendar.MINUTE, other);
		long difs = (long) diff(Calendar.SECOND, other);
		
		StringBuilder strb = new StringBuilder();
		strb.append(difd).append(" dias ");
		strb.append(difh -difd*24).append(" hrs ");
		strb.append(difm -difh*60).append(" min ");
		strb.append(difs -difm*60).append(" seg ");
		
		return strb.toString();
	}
	
	public void setFormat(DateFormat format) {

		switch (format) {
			case DD_MM_YYYY:
			case DD_MONTH_NAME_YYYY:
			case YYYY_MM_DD:
			case YYYY_MONTH_NAME_DD:
				this.calendar.set(Calendar.HOUR,   0);
				this.calendar.set(Calendar.MINUTE, 0);
				this.calendar.set(Calendar.SECOND, 0);
				this.calendar.set(Calendar.MILLISECOND, 0);
				break;
			default:
				break;
		
		}
		switch (format) {
			case DD_MONTH_NAME_YYYY:
			case DD_MONTH_NAME_YYYY_hh_mm_ss:
			case YYYY_MONTH_NAME_DD:
			case YYYY_MONTH_NAME_DD_hh_mm_ss:
			case E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone:
				separator = DateFormatSeparator.SPACE;
				break;
			default:
				if(separator == null)
					separator = DateFormatSeparator.HYPHEN;
				break;
		}
		this.format = format;
	}
	public void setSeparator(DateFormatSeparator separator) {
		this.separator = separator;
	}
	/**
	 * Compara los tiempos en milisegundos representados por los dos objetos DateTime
	 * @param date
	 * @return <br>el valor 0 si el tiempo representado es el mismo.<br>
	 * el valor -1 si el tiempo de este DateTime es antes del representado por el argumento.<br>
	 * el valor 1 si el tiempo de este DateTime es despues del representado por el argumento
	 */
	public int compareTo(DateTime date){
		long me = calendar.getTimeInMillis();
		long other = date.calendar.getTimeInMillis();
		
		if(me == other)
			return 0;
		if(me - other < 0)
			return -1;
		return 1;
	}
	/**
	 * Compara los tiempos en milisegundos representados por los dos objetos DateTime. Sólo considera los días completos para la comparación.
	 * @param date
	 * @return <br>el valor 0 si el tiempo representado es el mismo.<br>
	 * el valor -1 si el tiempo de este DateTime es antes del representado por el argumento.<br>
	 * el valor 1 si el tiempo de este DateTime es despues del representado por el argumento
	 */
	public int compareFullDaysTo(DateTime date){		
		
		long meMilis = getDayTimeInMillis();
		
		long comparableMilis = date.getDayTimeInMillis();
		
		if(meMilis == comparableMilis)
			return 0;
		if(meMilis - comparableMilis < 0)
			return -1;
		return 1;
	}
	
	/**
	 * @desc obtiene los milisegundos considerando solo el año, mes y día. Ignora el resto de parámetros
	 * @return long
	 */
	public long getDayTimeInMillis(){
		DateTime dayOnly = new DateTime(this.getYear(), this.getMonth(), this.getDay());
		return dayOnly.getCalendar().getTimeInMillis();
		
//		Calendar calendar = (Calendar) this.calendar.clone();
//		calendar.clear(Calendar.HOUR);
//		calendar.clear(Calendar.MINUTE);
//		calendar.clear(Calendar.SECOND);
//		calendar.clear(Calendar.MILLISECOND);
//		
//		return calendar.getTimeInMillis();
	}
	@Override
	public boolean equals(Object date) {
		if(date == null || !DateTime.class.isAssignableFrom(date.getClass()))
			return false;
		return compareTo((DateTime)date) == 0;
	}
	
	/**
	 * Obtiene la fecha actual en el formato YYYY-MM-DD hh:mm:ss, ejem:<br/>
	 * 23-01-2013
	 * @return
	 */
	public static DateTime getNow() {
		return new DateTime(DateFormat.YYYY_MM_DD_hh_mm_ss);
	}
	/**
	 * Obtiene la fecha y hora actual en  UTC+00
	 * @return
	 */
	public static DateTime getNowUTC() {
		DateTime now = new DateTime(DateFormat.YYYY_MM_DD_hh_mm_ss);
		now.calendar.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));;
		//now.calendar.gett
		return now;
	}
	
	/**
	 * Obtiene la hora actual
	 * @return
	 */
	public static String getNowTime() {
		Calendar now = Calendar.getInstance();
		return now.get(Calendar.HOUR_OF_DAY)+":"+now.get(Calendar.MINUTE)+":"+now.get(Calendar.SECOND);
	}
	
	/**
	 * Obtiene la fecha actual en el formato especificado
	 * @param format DateFormatSeparator: indica el formato de hora a utilizar, ejem:<br/>
	 *  - DD_MM_YYYY  23-01-2013<br/>
     *  - DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02<br/>
	 *  - YYYY-MM-DD 2013-01-23<br/>
	 *
	 * @return
	 */
	public static DateTime getNow(DateFormat format) {
		return new DateTime(format);		
	}
	/**
	 * Obtiene la fecha actual en el formato y con el separador de fecha especificado.
	 * 
	 * 
	 * @param format DateFormatSeparator: indica el formato de hora a utilizar, ejem:<br/>
	 *  - DD_MM_YYYY  23-01-2013<br/>
     *  - DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02<br/>
	 *  - YYYY-MM-DD 2013-01-23<br/>
	 * @param separator indica el caracter separador a usar en la fecha, ejem:<br/>
	 *  - DOT  23.01.2013<br/>
     *  - SLASH 23/01/2013 18:55:02<br/>
	 *  - HYPHEN 2013-01-23<br/>
	 * @return
	 */
	public static DateTime getNow(DateFormat format,DateFormatSeparator separator) {
		return new DateTime(format, separator);
	}

	/**
	 * Obtiene la fecha personalizada en el formato YYYY-MM-DD, ejem:<br/>
	 * 23-01-2013
	 * @return
	 */
	public static DateTime getCustom(int year,int month,int date) {
		return new DateTime(year,month,date);
	}
	/**
	 * Obtiene la fecha personalizada en el formato especificado
	 * @param format DateFormatSeparator: indica el formato de hora a utilizar, ejem:<br/>
	 *  - DD_MM_YYYY  23-01-2013<br/>
     *  - DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02<br/>
	 *  - YYYY-MM-DD 2013-01-23<br/>
	 *
	 * @return
	 */
	public static DateTime getCustom(int year,int month,int date,DateFormat format) {
		return new DateTime(year,month,date,format);		
	}
	/**
	 * Obtiene la fecha actual en el formato y con el separador de fecha especificado.
	 * 
	 * 
	 * @param format DateFormatSeparator: indica el formato de hora a utilizar, ejem:<br/>
	 *  - DD_MM_YYYY  23-01-2013<br/>
     *  - DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02<br/>
	 *  - YYYY-MM-DD 2013-01-23<br/>
	 * @param separator indica el caracter separador a usar en la fecha, ejem:<br/>
	 *  - DOT  23.01.2013<br/>
     *  - SLASH 23/01/2013 18:55:02<br/>
	 *  - HYPHEN 2013-01-23<br/>
	 * @return
	 */
	public static DateTime getCustom(int year,int month,int date,DateFormat format,DateFormatSeparator separator) {
		return new DateTime(year,month,date,format, separator);
	}
	/**
	 * Obtiene la fecha personalizada en el formato YYYY_MM_DD_hh_mm_ss, ejem:<br/>
	 * 23-01-2013
	 * @return
	 */
	public static DateTime getCustom(int year,int month,int date,int hour,int min,int sec) {
		return new DateTime(year, month, date, hour, min, sec);
	}

	
	/**
	 * Obtiene la fecha personalizada a aprtir de un String en el formato especificado
	 * @param source fecha string, se espera que este en el formato DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02 
	 *
	 * @return
	 * @throws Exception 
	 */
	//@ReportParameterParser
	public static DateTime getCustom(String source){
		return getCustom(source,DateFormat.YYYY_MM_DD_hh_mm_ss);
	}
	/**
	 * Obtiene la fecha personalizada a aprtir de un String en el formato especificado
	 * @param source fecha string
	 * @param sourceFormat DateFormatSeparator: indica el formato de hora del parametro string fecha, ejem:<br/>
	 *  - DD_MM_YYYY  23-01-2013<br/>
     *  - DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02<br/>
	 *  - YYYY-MM-DD 2013-01-23<br/>
	 *
	 * @return
	 * @throws Exception 
	 */
	public static DateTime getCustom(String strDate,DateFormat sourceFormat){
		//2013-10-04 16:21:48.422393
		if(strDate == null || (strDate = strDate.trim()).equals("") || !isParseable(strDate, sourceFormat.toString()))
			return null;
		
		int year;
		int month;
		int date;
		int hh = 0;
		int mm = 0;
		int ss = 0;
		String source = strDate.replace("-", "").replace(".", "").replace("/", "").replace(" ", "").replace(":", "").replace("de", " ");
		Pattern p;
		switch (sourceFormat) {
		case DD_MM_YYYY:
			date  = Integer.parseInt(source.substring(0,2));
			month = Integer.parseInt(source.substring(2,4))-1;
			year  = Integer.parseInt(source.substring(4,8));
			return new DateTime(year,month,date,sourceFormat);
		case DD_MM_YYYY_hh_mm_ss:
			date  = Integer.parseInt(source.substring(0,2));
			month = Integer.parseInt(source.substring(2,4))-1;
			year  = Integer.parseInt(source.substring(4,8));
			if(source.length() > 8){
				hh  = Integer.parseInt(source.substring(8,10));
				mm  = Integer.parseInt(source.substring(10,12));
				if(source.length() >= 14)
					ss  = Integer.parseInt(source.substring(12,14));
			}
			return new DateTime(year,month,date,hh,mm,ss,sourceFormat);
		case DD_MONTH_NAME_YYYY:
			String values[] = source.split(" ");
			year  = Integer.parseInt(values[2]);
			month = MONTHS.indexOf(values[1]);
			date  = Integer.parseInt(values[0]);
			return new DateTime(year,month,date,sourceFormat);
		case DD_MONTH_NAME_YYYY_hh_mm_ss:
			throw new RuntimeException("Format not implemented:"+sourceFormat);
		case YYYY_MM_DD:
			year  = Integer.parseInt(source.substring(0,4));
			month = Integer.parseInt(source.substring(4,6))-1;
			date  = Integer.parseInt(source.substring(6,8));
			return new DateTime(year,month,date,sourceFormat);
		case YYYY_MM_DD_hh_mm_ss:
			year  = Integer.parseInt(source.substring(0,4));
			month = Integer.parseInt(source.substring(4,6))-1;
			date  = Integer.parseInt(source.substring(6,8));
			if(source.length() > 8){
				hh  = Integer.parseInt(source.substring(8,10));
				mm  = Integer.parseInt(source.substring(10,12));
				if(source.length() >= 14)
					ss  = Integer.parseInt(source.substring(12,14));
			}
			return new DateTime(year,month,date,hh,mm,ss,sourceFormat);
		case E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone:
			p = Pattern.compile(REGEX_NAMED_FORMAT);
			Matcher mtc = p.matcher(strDate.replace("-", " "));
			if(mtc.find()) {
				date = Integer.parseInt(mtc.group(2));
				month = MONTHS_EN.indexOf( mtc.group(3));
				year = Integer.parseInt(mtc.group(4));
				hh = Integer.parseInt(mtc.group(5));
				mm = Integer.parseInt(mtc.group(6));
				ss = Integer.parseInt(mtc.group(7));
				
				DateTime d = new DateTime(year,month,date,hh,mm,ss,sourceFormat);
				d.calendar.setTimeZone(TimeZone.getTimeZone(mtc.group(8)));
				return d;
			}else {
				throw new RuntimeException("Mal formed format for:"+sourceFormat);
			}
		case YYYY_MONTH_NAME_DD:
		case YYYY_MONTH_NAME_DD_hh_mm_ss:
			throw new RuntimeException("Format not implemented:"+sourceFormat);
		default:
			break;
		}
		
		return null;		
	}
	/**
	 * Obtiene la fecha personalizada a aprtir de un String en el formato especificado
	 * @param source fecha string
	 * @param formatSource DateFormatSeparator: indica el formato de hora del parametro string fecha, ejem:<br/>
	 *  - DD_MM_YYYY  23-01-2013<br/>
     *  - DD_MM_YYYY_hh_mm_ss 23-01-2013 18:55:02<br/>
	 *  - YYYY-MM-DD 2013-01-23<br/>
	 * @param requiredFormat formato en que se desea visualizar la fecha
	 * @return
	 */
	public static DateTime getCustom(String source,DateFormat sourceFormat,DateFormat requiredFormat){
		DateTime date = getCustom(source, sourceFormat);
		date.setFormat(requiredFormat);
		return date;
	}
	
	@Override
	public int hashCode() {
		//return calendar.hashCode();
		return this.toString().hashCode();
	}

	/**
	 * Retorna la representacion de DateTime deacuerdo a los formatos de fecha y separador establecidos.<br>
	 * <strong>- Formato por defecto  :</strong> DD_MM_YYYY <br>
	 * <strong>- Separador por defecto:</strong> HYPHEN  <br>
	 * Ejemplo formato por defecto: 23-01-2013
	 */
	public String toString() {
		//TimeZone tz	 = calendar.getTimeZone();
	    DecimalFormat fmt = new DecimalFormat("00");
	    StringBuilder fecha = new StringBuilder();      
	    

	  
	    switch (format) {
		case DD_MM_YYYY:
		case DD_MM_YYYY_hh_mm_ss:
		    fecha.append(fmt.format(calendar.get(Calendar.DAY_OF_MONTH)));
		    fecha.append(separator).append(fmt.format(calendar.get(Calendar.MONTH) + 1));
		    fecha.append(separator).append(calendar.get(Calendar.YEAR));
			break;
		case DD_MONTH_NAME_YYYY:
		case DD_MONTH_NAME_YYYY_hh_mm_ss:
		    fecha.append(fmt.format(calendar.get(Calendar.DAY_OF_MONTH)));
		    fecha.append(separator).append(MONTHS.get(calendar.get(Calendar.MONTH)));
		    fecha.append(separator).append(calendar.get(Calendar.YEAR));
			break;
		case YYYY_MM_DD:
		case YYYY_MM_DD_hh_mm_ss:
		    fecha.append(calendar.get(Calendar.YEAR));
		    fecha.append(separator).append(fmt.format(calendar.get(Calendar.MONTH) + 1));
		    fecha.append(separator).append(fmt.format(calendar.get(Calendar.DAY_OF_MONTH)));
			break;
			
		case YYYY_MONTH_NAME_DD:
		case YYYY_MONTH_NAME_DD_hh_mm_ss:
		    fecha.append(calendar.get(Calendar.YEAR));
		    fecha.append(separator).append(MONTHS.get(calendar.get(Calendar.MONTH)));
		    fecha.append(separator).append(fmt.format(calendar.get(Calendar.DAY_OF_MONTH)));
			break;
		default:
			break;
			
		}
	    switch (format) {
			case DD_MM_YYYY_hh_mm_ss:
			case DD_MONTH_NAME_YYYY_hh_mm_ss:
			case YYYY_MM_DD_hh_mm_ss:
				if(separator != DateFormatSeparator.NONE){
				    fecha.append(" ").append(fmt.format(calendar.get(Calendar.HOUR_OF_DAY)));
				    fecha.append(":").append(fmt.format(calendar.get(Calendar.MINUTE)));
				    fecha.append(":").append(fmt.format(calendar.get(Calendar.SECOND)));
				}else{
				    fecha.append(fmt.format(calendar.get(Calendar.HOUR_OF_DAY)));
				    fecha.append(fmt.format(calendar.get(Calendar.MINUTE)));
				    fecha.append(fmt.format(calendar.get(Calendar.SECOND)));
				}
				break;
			default:
				break;
		}
	    switch (format) {
			case E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone:
				fecha.append(DAYS.get(calendar.get(Calendar.DAY_OF_WEEK)-1)); //día de la semana
				fecha.append(" ");
			    fecha.append(fmt.format(calendar.get(Calendar.DAY_OF_MONTH))); //día del mes
			    fecha.append(" de ");
			    fecha.append(MONTHS.get(calendar.get(Calendar.MONTH))); //mes
			    fecha.append(" de ");
			    fecha.append(calendar.get(Calendar.YEAR)); //año
			    fecha.append(", ");
			    fecha.append(fmt.format(calendar.get(Calendar.HOUR_OF_DAY))); //hora
			    fecha.append(":");
			    fecha.append(fmt.format(calendar.get(Calendar.MINUTE))); //minutos
			    fecha.append(":");
			    fecha.append(fmt.format(calendar.get(Calendar.SECOND))); //días
			    fecha.append(" ");
			    fecha.append(calendar.getTimeZone().getDisplayName());
				break;
			default:
				break;
		}
	    //fecha.append(" ").append(tz.getRawOffset()/(1000*60*60) );
	    return fecha.toString();
	  }
	
	public String toString(DateFormat format){
		DateFormat current = this.format;
		Calendar currentetCalendar = (Calendar)this.calendar.clone();
		this.setFormat(format);
		String value = this.toString();
		this.calendar = currentetCalendar;
		this.setFormat(current);
		return value;
		
	}
	
	public boolean isLastDayOf(int value){
		
		if(value == Calendar.DAY_OF_WEEK)
			return this.calendar.get(Calendar.DAY_OF_WEEK) == 1;
		if(value == Calendar.DAY_OF_MONTH)
			return this.calendar.get(Calendar.DAY_OF_MONTH) == this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(value == Calendar.DAY_OF_YEAR )
			return this.calendar.get(Calendar.MONTH) == 11 && this.calendar.get(Calendar.DAY_OF_MONTH) == 31;
		return false;
	}
	public static boolean isParseable(String dateTime, String dateFromat){
		 
		//TODO:por implementar
 
		return true;
	}
	
	
	public static void main(String[] args) {
		System.out.println(getNow().getCalendar().get(Calendar.WEEK_OF_YEAR));
		
		System.out.println("isLastDayOf:");
		DateTime d1 = getCustom("2014-04-12",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_WEEK:"+d1.isLastDayOf(Calendar.DAY_OF_WEEK));
		d1 = getCustom("2014-04-13",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_WEEK:"+d1.isLastDayOf(Calendar.DAY_OF_WEEK));
		d1 = getCustom("2014-04-20",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_WEEK:"+d1.isLastDayOf(Calendar.DAY_OF_WEEK));
		d1 = getCustom("2014-04-20",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_MONTH:"+d1.isLastDayOf(Calendar.DAY_OF_MONTH));
		d1 = getCustom("2014-04-30",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_MONTH:"+d1.isLastDayOf(Calendar.DAY_OF_MONTH));
		d1 = getCustom("2014-04-31",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_MONTH:"+d1.isLastDayOf(Calendar.DAY_OF_MONTH));
		d1 = getCustom("2014-04-31",DateFormat.YYYY_MM_DD);
		System.out.println(d1.addDays(-1)+" Calendar.DAY_OF_YEAR:"+d1.isLastDayOf(Calendar.DAY_OF_YEAR));
		d1 = getCustom("2014-12-31",DateFormat.YYYY_MM_DD);
		System.out.println(d1+" Calendar.DAY_OF_YEAR:"+d1.isLastDayOf(Calendar.DAY_OF_YEAR));
		
		
		
		double dif  = getCustom("2008-07-02",DateFormat.YYYY_MM_DD).diffDays(getCustom("2013-11-13",DateFormat.YYYY_MM_DD));
		System.out.println(dif);
		d1  = getCustom("2013-10-30 16:21:48.422393", DateFormat.YYYY_MM_DD_hh_mm_ss);
		DateTime now = getNow();

		System.out.println("diference sec   "+d1+" - now():"+d1.diffSeconds(now));
		System.out.println("diference min   "+d1+" - now():"+d1.diffMinutes(now));
		System.out.println("diference hour  "+d1+" - now():"+d1.diffHours(now));
		System.out.println("diference days  "+d1+" - now():"+d1.diffDays(now));	
		
		System.out.println("diference dd-hh-mm-ss "+d1+" - now():"+d1.diffDDHHMMSS(now));		
		System.out.println("add 5 days to now():"+getNow().addDays(5));		
		System.out.println("add 5 seconds to string default:"+getCustom("2013-10-28 16:21:48.422393", DateFormat.YYYY_MM_DD_hh_mm_ss).addSeconds(5));
		System.out.println("add 5 minutes to string default:"+getCustom("2013-10-28 16:21:48.422393", DateFormat.YYYY_MM_DD_hh_mm_ss).addMinutes(5));
		System.out.println("add 5 hours to string default:"+getCustom("2013-10-28 16:21:48.422393", DateFormat.YYYY_MM_DD_hh_mm_ss).addHours(5));
		System.out.println("add 5 days to string default:"+getCustom("2013-10-28 16:21:48.422393").addDays(5));
		System.out.println("add 5 months to string default:"+getCustom("2013-10-28 16:21:48.422393").addMonths(5));
		System.out.println("add 5 years to string default:"+getCustom("2013-10-28 16:21:48.422393").addYears(5));
		
		System.out.println("from string default:"+getCustom("2013-10-04 16:21:48.422393"));
		System.out.println("from string       :"+getCustom("2013-10-04 16:21:48.422393", DateFormat.YYYY_MM_DD_hh_mm_ss));
		System.out.println("from string       :"+getCustom("2013-10-04 16:21:48.422393", DateFormat.YYYY_MM_DD));
		
		System.out.println("from string parse:"+getCustom("2013-10-04 16:21:48.422393", DateFormat.YYYY_MM_DD_hh_mm_ss,DateFormat.DD_MM_YYYY_hh_mm_ss));
		System.out.println("from string parse:"+getCustom("2013-10-04 16:21:48.422393", DateFormat.YYYY_MM_DD,DateFormat.DD_MM_YYYY));
		
		System.out.println("from string:"+getCustom("04-10-2013 16:21:48.422393", DateFormat.DD_MM_YYYY));
		System.out.println("from string:"+getCustom("04-10-2013 16:21:48.422393", DateFormat.DD_MM_YYYY_hh_mm_ss));
		
		System.out.println("DEFAULT FORMAT:"+getNow());

		System.out.println("\nUSING FORMATS:");
		System.out.println("DD_MM_YYYY          : "+getNow(DateFormat.DD_MM_YYYY));
		System.out.println("DD_MM_YYYY_hh_mm_ss : "+getNow(DateFormat.DD_MM_YYYY_hh_mm_ss));
		System.out.println("YYYY_MM_DD          : "+getNow(DateFormat.YYYY_MM_DD));
		System.out.println("YYYY_MM_DD_hh_mm_ss : "+getNow(DateFormat.YYYY_MM_DD_hh_mm_ss));
		
		System.out.println("\nUSING SEPARATOR:");
		System.out.println("HYPHEN: "+getNow(DateFormat.DD_MM_YYYY,DateFormatSeparator.HYPHEN)+"  |  "+getNow(DateFormat.DD_MM_YYYY_hh_mm_ss,DateFormatSeparator.HYPHEN));
		System.out.println("DOT   : "+getNow(DateFormat.DD_MM_YYYY,DateFormatSeparator.DOT)+"  |  "+getNow(DateFormat.DD_MM_YYYY_hh_mm_ss,DateFormatSeparator.DOT));
		System.out.println("SLASH : "+getNow(DateFormat.DD_MM_YYYY,DateFormatSeparator.SLASH)+"  |  "+getNow(DateFormat.DD_MM_YYYY_hh_mm_ss,DateFormatSeparator.SLASH));
		System.out.println("NONE  : "+getNow(DateFormat.DD_MM_YYYY,DateFormatSeparator.NONE)+"    |  "+getNow(DateFormat.DD_MM_YYYY_hh_mm_ss,DateFormatSeparator.NONE));
	
		System.out.println("\nUSING getCustom(1985, 6, 5): ");
		DateTime custom = getCustom(1985, 6, 5);
		System.out.println("DEFAULT FORMAT               : "+custom);
		
		custom.setFormat(DateFormat.DD_MM_YYYY_hh_mm_ss);
		System.out.println("DD_MM_YYYY_hh_mm_ss          : "+custom);

		custom.setFormat(DateFormat.DD_MONTH_NAME_YYYY);
		System.out.println("DD_MONTH_NAME_YYYY           : "+custom);
		custom.setFormat(DateFormat.DD_MONTH_NAME_YYYY_hh_mm_ss);
		System.out.println("DD_MONTH_NAME_YYYY_hh_mm_ss  : "+custom);
		
		custom = getCustom(1985, 6, 5,10,5,12);
		System.out.println("getCustom(1985, 6, 5,10,5,12):"+custom);

		

		System.out.println("\nCOMPARE:");
		now = getNow();
		custom = getNow();
		if(custom.compareTo(now) == 0){
			System.out.println(custom +" igual a " + now);
		}
		now.setFormat(DateFormat.DD_MM_YYYY_hh_mm_ss);
		custom = getCustom(now.getYear(), now.getMonth(), now.getDay(),0,0,0);
		if(custom.compareTo(now) < 0){
			System.out.println(custom +" menor a " + now);
		}
		custom = getCustom(now.getYear(), now.getMonth(), now.getDay()+1,0,0,0);
		if(custom.compareTo(now) > 0){
			System.out.println(custom +" mayor a " + now);
		}
	}

	public void setTimeZone(TimeZone timeZone) {
		calendar.setTimeZone(timeZone);		
	}
	public static DateFormat inferFormat(String date) {
		//((\d{2})(\/|-|\s)(\d{2})(\/|-|\s)(\d{4}))|((\d{4})(\/|-|\s)(\d{2})(\/|-|\s)(\d{2}))(\s(\d{2}):(\d{2}):(\d{2}))?
		String DDMMYYY = "^((0[0-9])|[1-2][0-9]|30|31)(\\/|-|\\s)(0[1-9]|10|11|12)(\\/|-|\\s)(\\d{4})$";
		String DDMMYYY_HHMMSS = "^((0[0-9])|[1-2][0-9]|30|31)(\\/|-|\\s)(0[1-9]|10|11|12)(\\/|-|\\s)(\\d{4})(\\s(\\d{2}):(\\d{2}):(\\d{2}))$";
		

		String YYYYMMDD = "^(\\d{4})(\\/|-|\\s)(0[1-9]|10|11|12)(\\/|-|\\s)((0[0-9])|[1-2][0-9]|30|31)$";
		String YYYYMMDD_HHMMSS = "^(\\d{4})(\\/|-|\\s)(0[1-9]|10|11|12)(\\/|-|\\s)((0[0-9])|[1-2][0-9]|30|31)((\\s|T)(\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?)$";
		

		Pattern p = Pattern.compile(DDMMYYY);
		Matcher m = p.matcher(date);		
		if(m.find())
			return DateFormat.DD_MM_YYYY;

		p = Pattern.compile(DDMMYYY_HHMMSS);
		m = p.matcher(date);		
		if(m.find())
			return DateFormat.DD_MM_YYYY_hh_mm_ss;

		p = Pattern.compile(YYYYMMDD);
		m = p.matcher(date);		
		if(m.find())
			return DateFormat.YYYY_MM_DD;

		p = Pattern.compile(YYYYMMDD_HHMMSS);
		m = p.matcher(date);		
		if(m.find()) {
			return DateFormat.YYYY_MM_DD_hh_mm_ss;
		}
		
		p = Pattern.compile(REGEX_NAMED_FORMAT);
		m = p.matcher(date);		
		if(m.find())
			return DateFormat.E_DD_MONTH_NAME_YYYY_HH_mm_ss_time_zone;
		
		return null;
	}
}
