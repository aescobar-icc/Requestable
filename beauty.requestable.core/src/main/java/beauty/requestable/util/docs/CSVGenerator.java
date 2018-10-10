package beauty.requestable.util.docs;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import beauty.requestable.util.reflection.UtilReflection;
import beauty.requestable.util.string.StringBuilderUtil;

public class CSVGenerator {
	
	private List<TableHeader> headers = new ArrayList<TableHeader>();
	private List<?> values;
	public CSVGenerator() {
	}
	public void setValues(List<?> values){
		this.values = values;
	}
	public String genera(OutputStream out) throws Exception{

		if(headers.size() == 0)
			throw new Exception("Headers is not defined");
		if(values == null || values.size() == 0)
			return "";

		
		StringBuilder strb = new StringBuilder();
		
		for(TableHeader header: headers){
			strb.append(header.getHeaderName()).append(";");
		}
		StringBuilderUtil.replaceLast(strb, ";", "\n");

		for(Object obj:values){	
			for(TableHeader header: headers){
				if(header.getType().equals("text"))
					strb.append("'").append(UtilReflection.getFieldValue(obj.getClass().getDeclaredField(header.getFieldName()), obj)).append("'").append(";");
				else
					strb.append(UtilReflection.getFieldValue(obj.getClass().getDeclaredField(header.getFieldName()), obj)).append(";");
			}
			StringBuilderUtil.replaceLast(strb, ";", "\n");
			
			StringBuilderUtil.replace(strb, "null", "");
			out.write(strb.toString().getBytes());
			strb.setLength(0);
		}
		return strb.toString();
	}	

	
	public void putHeader(String fieldName,String headerName){
		headers.add(new TableHeader(fieldName, headerName));
	}

}


