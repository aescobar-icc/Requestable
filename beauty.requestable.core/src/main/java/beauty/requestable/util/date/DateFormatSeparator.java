package beauty.requestable.util.date;

public enum DateFormatSeparator {
	DOT("."),
	SLASH("/"),
	HYPHEN("-"),
	SPACE(" "),
	NONE("");
	private String value;
	DateFormatSeparator(String value){
		this.value = value;
	}
	public String toString(){
		return value;
	}
}
