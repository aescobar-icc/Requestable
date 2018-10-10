package beauty.requestable.util.format;



public class FormatoRut {
	
	static public String formatear(String rut){
        int cont=0;
        String format;
        rut = rut.replace(".", "");
        rut = rut.replace("-", "");
        format = "-"+rut.substring(rut.length()-1);
        for(int i = rut.length()-2;i>=0;i--){
            format = rut.substring(i, i+1)+format;
            cont++;
            if(cont == 3 && i != 0){
                format = "."+format;
                cont = 0;
            }
        }
        return format;
    }	

}
