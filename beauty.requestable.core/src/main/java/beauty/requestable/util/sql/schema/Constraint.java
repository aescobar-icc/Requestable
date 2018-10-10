package beauty.requestable.util.sql.schema;

import java.util.ArrayList;
import java.util.List;


public class Constraint {
	private int order;
	private String type;
	private String name;
	private String tableName;
	private List<String> tableColumns = new ArrayList<String>();
	private String foreingTableName;
	private List<String> foreingTableColumns = new ArrayList<String>();
	

	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<String> getTableColumns() {
		return tableColumns;
	}
	public void setTableColumns(List<String> tableColumns) {
		this.tableColumns = tableColumns;
	}
	public String getForeingTableName() {
		return foreingTableName;
	}
	public void setForeingTableName(String foreingTableName) {
		this.foreingTableName = foreingTableName;
	}
	public List<String> getForeingTableColumns() {
		return foreingTableColumns;
	}
	public void setForeingTableColumns(List<String> foreingTableColumns) {
		this.foreingTableColumns = foreingTableColumns;
	}
	
	
}
