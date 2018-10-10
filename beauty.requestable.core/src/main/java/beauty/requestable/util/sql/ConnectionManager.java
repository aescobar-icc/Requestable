package beauty.requestable.util.sql;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import beauty.requestable.util.properties.PropertiesException;

public class ConnectionManager {
	private DBProperties dbProperties;
	private Connection connection;
	

	public ConnectionManager(String propertiesPath,boolean useCatalinaBase, boolean encrypted) throws ConnectionManagerException {
		try {
			dbProperties = new DBProperties(propertiesPath, useCatalinaBase, encrypted);
		} catch (PropertiesException e) {
			throw new ConnectionManagerException("Error loading properties",e);
		}		
	}
	public ConnectionManager(InputStream properties) throws ConnectionManagerException {
		try {
			dbProperties = new DBProperties(properties);
		} catch (PropertiesException e) {
			throw new ConnectionManagerException("Error loading properties",e);
		}		
	}
	public Connection getConnection() throws ConnectionManagerException{
	//	String URL_CONNECTION = "jdbc:postgresql://10.1.1.20:5432/pcfactory";
	//	String user="postgres";
	//	String password="plokijuh"; 
		try {
			if( connection != null && !connection.isClosed()) 
				return connection;
		} catch (SQLException e1) {
		}
		
		try { 
			//System.out.println(dbProperties);
			Class.forName(dbProperties.getDriver()); 
			connection = DriverManager.getConnection(dbProperties.getURL(),dbProperties.getUser(),dbProperties.getPassword()); 
		} catch (Exception e) { 
			throw new ConnectionManagerException("Error creating connection",e);
		} 
 
		System.out.println("Database connection successfully");
		return connection;
	}

	public void closeConnection() throws ConnectionManagerException{
		if(connection!=null){
			try {
				connection.close();
			} catch (SQLException e) {
				throw new ConnectionManagerException("Data base access error",e);
			}finally {
				connection = null;
			}
		}
		System.out.println("Database connection close");
	}
	public boolean isClosed() throws ConnectionManagerException {
		if(connection!=null){
			try {
				return connection.isClosed();
			} catch (SQLException e) {
				throw new ConnectionManagerException("Data base access error",e);
			}
		}
		return true;
	}

}
