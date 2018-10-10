package beauty.requestable.core.enums;

public enum RequestableType {
	WEB_PAGE,SERVICE,COMPONENT,DOWNLOAD_FILE_WRITER;
	
	public static RequestableType getByName(String name){
		
		for(RequestableType val :RequestableType.values()){
			if(val.toString().equals(name))
				return val;
		}
		return null;
	}
}
