package beauty.requestable.util.sql.postgres.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import beauty.requestable.util.sql.schema.Constraint;
import beauty.requestable.util.sql.schema.DataType;
import beauty.requestable.util.sql.schema.IDaoSchema;
import beauty.requestable.util.sql.schema.Table;

public class DaoPostgresSchema implements IDaoSchema{
	public static final String PRIMARY_KEY ="PRIMARY KEY";
	public static final String FOREIGN_KEY ="FOREIGN KEY";

	private Connection conn=null;
	public DaoPostgresSchema(Connection conn) {
		this.conn = conn;
	}
	
	public Table getTableSchema(String tableName) throws SQLException {
		Table tableDef = new Table();
		tableDef.setName(tableName);
		List<Constraint> pk = getConstraints(tableName, PRIMARY_KEY);
		if(pk.size() > 0) {
			tableDef.setPrimaryConstraint(pk.get(0));
		}
		tableDef.setForeingConstraints(getConstraints(tableName, FOREIGN_KEY));
		
		String query =  "SELECT column_name,data_type,character_maximum_length,numeric_precision,numeric_scale " +
						"FROM information_schema.columns " +
						"WHERE table_name=?" +
						"ORDER BY ordinal_position";
		
		PreparedStatement pstmt=null;
		ResultSet result=null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, tableName);
			result = pstmt.executeQuery();
			while (result.next()) {

				tableDef.getColumns().add(result.getString("column_name"));
				tableDef.getTypes()
						.add(new DataType(	result.getString("data_type"), 
											result.getString("character_maximum_length"),
											result.getString("numeric_precision"), 
											result.getString("numeric_scale")));
			}
		} finally {
			close(pstmt, result);
		}
		return tableDef;
	}
	public List<Constraint> getConstraints(String tableName,String constrainType) throws SQLException {
		String query =	"SELECT" + 
						"    tc.table_schema, " + 
						"    tc.constraint_name, " + 
						"    tc.table_name, " + 
						"    kcu.column_name, " + 
						"    ccu.table_schema AS foreign_table_schema," + 
						"    ccu.table_name AS foreign_table_name," + 
						"    ccu.column_name AS foreign_column_name " + 
						"FROM " + 
						"    information_schema.table_constraints AS tc " + 
						"    JOIN information_schema.key_column_usage AS kcu" + 
						"      ON tc.constraint_name = kcu.constraint_name" + 
						"      AND tc.table_schema = kcu.table_schema" + 
						"    JOIN information_schema.constraint_column_usage AS ccu" + 
						"      ON ccu.constraint_name = tc.constraint_name" + 
						"      AND ccu.table_schema = tc.table_schema " + 
						"WHERE constraint_type = ? AND tc.table_name=?;";
		HashMap<String, Constraint> hc = new HashMap<>();
		List<Constraint> constraints = new ArrayList<Constraint>();
		PreparedStatement pstmt=null;
		ResultSet result=null;
		Constraint constraint;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, constrainType);
			pstmt.setString(2, tableName);
			//System.out.println(pstmt.toString());
			result = pstmt.executeQuery();
			while(result.next()){
				constraint = null;
				String name = result.getString("constraint_name");
				if(hc.containsKey(name)) {
					constraint = hc.get(name);
				}else{
					constraint = new Constraint();
					constraint.setName(name);
					constraint.setTableName(result.getString("constraint_name"));
					constraint.setForeingTableName(result.getString("foreign_table_name"));
					constraints.add(constraint);
					hc.put(name, constraint);
				}
				constraint.getTableColumns().add(result.getString("column_name"));
				constraint.getForeingTableColumns().add(result.getString("foreign_column_name"));
			}
		}finally{
			close(pstmt,result);
		}
		return constraints;
	}
	public List<Table> listTablesSchema(String schema)  throws SQLException {
		
		String query = "SELECT * FROM information_schema.tables WHERE table_schema = ?";
		List<Table> tables = new ArrayList<Table>();
		PreparedStatement pstmt=null;
		ResultSet result=null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, schema);
			result = pstmt.executeQuery();
			while(result.next()){
				
				tables.add(new Table( result.getString("table_name")));
			}
		}finally{
			close(pstmt,result);
		}
		return tables;
		
	}
	private void close(PreparedStatement pstmt, ResultSet result) {
		if (result != null)
			try {
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (pstmt != null)
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

	}

}
