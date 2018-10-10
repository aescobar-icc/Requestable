package beauty.requestable.util.sql.postgres;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Function {
	private int oid;
	private String name;
	private String returnType;
	private Integer outerParamType;
	private List<Type> parameters = new ArrayList<Type>();	

	private boolean nonAutoCommitNeeded = false;

	public int getOid() {
		return oid;
	}
	public void setOid(int oid) {
		this.oid = oid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public Integer getOuterParamType() {
		return outerParamType;
	}
	public void setOuterParamType(Integer outerParamType) {
		this.outerParamType = outerParamType;
	}
	public List<Type> getParameters() {
		return parameters;
	}
	public void addParameter(Type p) {
		this.parameters.add(p);
	}
	@Override
	public String toString() {
		return String.format("Function %s(%s):%s", name,
				parameters!=null ?Arrays.toString(parameters.toArray()).replace("[", "").replace("]", ""):"", returnType );
	}
	public boolean isNonAutoCommitNeeded() {
		return nonAutoCommitNeeded;
	}
	public void setNonAutoCommitNeeded(boolean nonAutoCommitNeeded) {
		this.nonAutoCommitNeeded = nonAutoCommitNeeded;
	}
	
	
	
}
