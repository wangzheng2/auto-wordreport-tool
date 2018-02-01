package core.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;

import core.generator.ReportGenerator;

public class DbDataSource extends DataSource {

	private String driver = null;
	private String url = null;
	private String username = null;
	private String password = null;
	private Connection conn = null;
	Logger logger = ReportGenerator.getLogger();
	
	public Connection getConnection() throws ClassNotFoundException, SQLException {
		if (conn == null) {
			Class.forName(this.getDriver());
            conn = DriverManager.getConnection(this.getUrl());
            if(conn != null)
            	logger.info("DB Connection successful: " + this.getUrl());
		}
		return conn;	
	}
	
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public DbDataSource(String name, String driver, String url, String username, String password) {
		super(name, DB);
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		//reuse hierarchical visitor here, which has enough ability to visit db data here.
		setVisitor(new HierarchyVisitor(this));
	}

}
