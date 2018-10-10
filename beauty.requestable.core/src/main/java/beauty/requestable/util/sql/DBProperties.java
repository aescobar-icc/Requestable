package beauty.requestable.util.sql;

import java.io.InputStream;

import beauty.requestable.util.properties.PropertiesException;
import beauty.requestable.util.properties.PropertiesLoader;


public class DBProperties extends PropertiesLoader {
	
	public DBProperties(String path,boolean useCatalinaBase,boolean encrypted) throws PropertiesException{
		super(path, useCatalinaBase, encrypted);
	}
	public DBProperties(InputStream input) throws PropertiesException{
		super(input);
	}

	public String getUser() throws PropertiesException{
		return properties.getProperty("user");
	}
	public String getPassword() throws PropertiesException{
		return properties.getProperty("password");
	}
	public String getURL() throws PropertiesException{
		return properties.getProperty("url");
	}
	public String getDriver() throws PropertiesException{
		return properties.getProperty("driver");
	}

	@Override
	public String toString() {
		try {
			return String
					.format("DBProperties [getUser()=%s, getPassword()=%s, getURL()=%s, getDriver()=%s]",
							getUser(), getPassword(), getURL(), getDriver());
		} catch (PropertiesException e) {
			return "DBProperties.toString throw PropertiesException" + e.toString();
		}
	}

}
