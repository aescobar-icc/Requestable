package beauty.requestable.util.format;

import java.util.Calendar;


public class FormatoFecha {
	
public static String DiferenciaFechasCompleta(String dInicio, String dFinal, boolean tiempo){
	String salida = "";
	Calendar start = Calendar.getInstance();
	
    int anio, mes, dia, hora, min, seg;
    anio = Integer.parseInt(dInicio.substring(0, 4));
    mes  = Integer.parseInt(dInicio.substring(5, 7));
    dia  = Integer.parseInt(dInicio.substring(8, 10));
    if (!tiempo){
    	hora=min=seg = 0;
    }else{
   	 	hora  = Integer.parseInt(dInicio.substring(11, 13));
   	 	min= Integer.parseInt(dInicio.substring(14, 16));
   	 	seg=Integer.parseInt(dInicio.substring(17, 19));
    }
    
    start.set(anio, mes, dia, hora, min, seg); 

    anio = Integer.parseInt(dFinal.substring(0, 4));
    mes  = Integer.parseInt(dFinal.substring(5, 7));
    dia  = Integer.parseInt(dFinal.substring(8, 10));
    if (!tiempo){
    	hora=min=seg = 0;
    }else{
   	 	hora  = Integer.parseInt(dFinal.substring(11, 13));
   	 	min= Integer.parseInt(dFinal.substring(14, 16));
   	 	seg=Integer.parseInt(dFinal.substring(17, 19));
    }

    
    
    Calendar end = Calendar.getInstance();
    
        
    end.set(anio, mes, dia, hora, min, seg);

    Integer[] elapsed = new Integer[6];
    Calendar clone = (Calendar) start.clone(); 
    elapsed[0] = elapsed(clone, end, Calendar.YEAR);
    System.out.println("clone: " + clone);
    System.out.println("END: " + end);
    System.out.println("calendar.year: " + Calendar.YEAR);
    clone.add(Calendar.YEAR, elapsed[0]);

    elapsed[1] = elapsed(clone, end, Calendar.MONTH);
    clone.add(Calendar.MONTH, elapsed[1]);
    elapsed[2] = elapsed(clone, end, Calendar.DATE);
    clone.add(Calendar.DATE, elapsed[2]);
    elapsed[3] = (int) (end.getTimeInMillis() - clone.getTimeInMillis()) / 3600000;
    clone.add(Calendar.HOUR, elapsed[3]);

    elapsed[4] = (int) (end.getTimeInMillis() - clone.getTimeInMillis()) / 60000;
    clone.add(Calendar.MINUTE, elapsed[4]);
    elapsed[5] = (int) (end.getTimeInMillis() - clone.getTimeInMillis()) / 1000;

    //System.out.format("%d Años, %d Meses, %d dias, %d horas, %d minutos, %d seconds", elapsed);

    if (elapsed[0]>0){
    	if (elapsed[0] > 1 )
    		salida += String.valueOf(elapsed[0]) + " Años ";
    	else
    		salida += String.valueOf(elapsed[0]) + " Año ";
    }
    
    if (elapsed[1]>0){
    	if (elapsed[1] > 1 )
    		salida += String.valueOf(elapsed[1]) + " Meses ";
    	else
    		salida += String.valueOf(elapsed[1]) + " Mes ";
    }
    
    if (elapsed[2]>0){
    	if (elapsed[2] > 1 )
    		salida += String.valueOf(elapsed[2]) + " Días ";
    	else
    		salida += String.valueOf(elapsed[2]) + " Día ";
    }
    
    
    if (tiempo){
    	salida  += String.valueOf(elapsed[3]) + ":" + String.valueOf(elapsed[4]) + ":" + String.valueOf(elapsed[5]);// + " Segundos";
    }
    
    // String.valueOf(elapsed[0]) + " Años, " + String.valueOf(elapsed[1]) + " Meses, " + String.valueOf(elapsed[2]) + " Dias, " + String.valueOf(elapsed[3]) + " Horas, " + String.valueOf(elapsed[4]) + " Minutos, " + String.valueOf(elapsed[5]) + " Segundos";
	
	return salida;
	
	}
      

private static int elapsed(Calendar before, Calendar after, int field) {
    Calendar clone = (Calendar) before.clone(); // Otherwise changes are been reflected.
    int elapsed = -1;
    while (!clone.after(after)) {
        clone.add(field, 1);
        elapsed++;
    }
    return elapsed;
}


}
