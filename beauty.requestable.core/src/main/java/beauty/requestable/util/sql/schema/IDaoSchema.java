package beauty.requestable.util.sql.schema;

import java.sql.SQLException;
import java.util.List;

public interface IDaoSchema {

	public Table getTableSchema(String tableName) throws SQLException;
	public List<Constraint> getConstraints(String tableName,String constrainType) throws SQLException ;
	public List<Table> listTablesSchema(String schema) throws SQLException ;
}
