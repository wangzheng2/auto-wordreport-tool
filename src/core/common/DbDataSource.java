package core.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;

import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * 关系型数据库数据源类
 * @author 朴勇 15641190702
 * 
 */
public class DbDataSource extends DataSource {
	//驱动
	private String driver = null;
	//URL
	private String url = null;
	//用户名
	private String username = null;
	//密码
	private String password = null;
	//连接
	private Connection conn = null;
	Logger logger = ReportGenerator.getLogger();
	
	//获取连接
	public Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName(this.getDriver());
				conn = DriverManager.getConnection(this.getUrl(), this.getUsername(), this.getPassword());
				if (conn != null)
					logger.info("DB Connection successful: " + this.getUrl());
			} catch (ClassNotFoundException e) {
				logger.error("Can not find DB driver: "+this.getName(), e);
			} catch (Exception e) {
				logger.error("Can not create connection: "+this.getName(), e);
			}
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
		//这里重用hierarchical访问器，具有足够的针对关系数据结构的访问能力。
		setVisitor(new HierarchyVisitor(this));
	}

	@Override
	public void cleanUp() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("Can not close database connection: "+this.getName(), e);
			}
		}
	}
}
