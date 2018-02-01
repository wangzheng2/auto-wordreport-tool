package core.generator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.DbDataSource;

public class TestDbDataSource {
	private DataSourceConfig dsc = DataSourceConfig.newInstance();
	@BeforeClass
	public static void setAllUp() {
		DataSourceConfigProcessor dsch = new DataSourceConfigProcessor("resource/Template_new_datasource.xml");		
		dsch.parseConfigFile();
	}
	
	@Before
	public void setup() {
		
	}

	@Test
	public void getConnection() throws ClassNotFoundException, SQLException {
		DataSource ds = dsc.getDataSource("db_sql");
		DbDataSource dbs = (DbDataSource)ds;
		assertNotNull("DB DataSource is NULL!",ds);
		assertTrue("driver is not correct!", "org.sqlite.JDBC".equalsIgnoreCase(dbs.getDriver()));
		String url = "jdbc:sqlite:/home/piao/piaoyong/UWR/00-客户提供资料/SpecChecker报告导出/数据源/RuleConLevT.db";
		assertTrue("URL is not correct!", url.equalsIgnoreCase( dbs.getUrl()));
		assertNotNull("DB Connection failed!",dbs.getConnection());
	}
}
