package beauty.requestable.util.sql.schema;
import java.util.ArrayList;
import java.util.List;


public class Table {

	private String name;
	private List<String> columns = new ArrayList<String>();
	private Constraint primaryConstraint;
	private List<Constraint> foreingConstraints = new ArrayList<Constraint>();
	private List<DataType> types = new ArrayList<DataType>();

	public Table() {
	}

	public Table(String name) {
		this.name=name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<DataType> getTypes() {
		return types;
	}

	public void setTypes(List<DataType> types) {
		this.types = types;
	}

	public Constraint getPrimaryConstraint() {
		return primaryConstraint;
	}

	public void setPrimaryConstraint(Constraint primaryConstraint) {
		this.primaryConstraint = primaryConstraint;
	}

	public List<Constraint> getForeingConstraints() {
		return foreingConstraints;
	}

	public void setForeingConstraints(List<Constraint> foreingConstraints) {
		this.foreingConstraints = foreingConstraints;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ").append(name).append("{\n");
		int cc = columns.size();
		for(int i=0;i<cc;i++) {
			builder.append("\t").append(columns.get(i)).append(" ").append(types.get(i).getDataType()).append(",\n");
		}

		if(primaryConstraint != null) {
			List<String> pkColumns = primaryConstraint.getTableColumns();
			cc = pkColumns.size();
			builder.append("\t CONSTRAINT ").append(primaryConstraint.getName()).append(" PRIMARY KEY (");
			for(int i=0;i<cc;i++) {
				builder.append(pkColumns.get(i));
				if(i<cc-1)
					builder.append(",");
			}
			builder.append(")\n");
		}
		for(Constraint c:foreingConstraints){
			List<String> tColumns = c.getTableColumns();
			cc = tColumns.size();
			builder.append("\t CONSTRAINT ").append(c.getName()).append(" FOREIGN KEY (");
			for(int i=0;i<cc;i++) {
				builder.append(tColumns.get(i));
				if(i<cc-1)
					builder.append(",");
			}
			builder.append(")\n\t\t REFERENCES ").append(c.getForeingTableName()).append("(");
			List<String> fkColumns = c.getForeingTableColumns();
			for(int i=0;i<cc;i++) {
				builder.append(fkColumns.get(i));
				if(i<cc-1)
					builder.append(",");
			}
			builder.append(")\n");
		}
		//append(columns).append(", primaryColumns=")
		//		.append(primaryColumns).append(", foreingConstraints=").append(foreingConstraints).append(", types=")
		builder.append("}");
		return builder.toString();
	}
	
	

}
