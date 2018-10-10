package beauty.requestable.util.sql.schema;

public class DataType {
	private String dataType;
	private String characterMaximumLength;
	private String numericPrecision;
	private String numericScale;
	
	public DataType() {
		// TODO Auto-generated constructor stub
	}
	
	
	public DataType(String dataType, String characterMaximumLength,String numericPrecision, String numericScale) {
		super();
		this.dataType = dataType;
		this.characterMaximumLength = characterMaximumLength;
		this.numericPrecision = numericPrecision;
		this.numericScale = numericScale;
	}


	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getCharacterMaximumLength() {
		return characterMaximumLength;
	}
	public void setCharacterMaximumLength(String characterMaximumLength) {
		this.characterMaximumLength = characterMaximumLength;
	}
	public String getNumericPrecision() {
		return numericPrecision;
	}
	public void setNumericPrecision(String numericPrecision) {
		this.numericPrecision = numericPrecision;
	}
	public String getNumericScale() {
		return numericScale;
	}
	public void setNumericScale(String numericScale) {
		this.numericScale = numericScale;
	}
	
	@Override
	public boolean equals(Object obj) {
		return dataType.equals(obj);
	}
	@Override
	public String toString() {
		return dataType;
	}

}
