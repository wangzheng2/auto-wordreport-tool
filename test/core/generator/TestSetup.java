package core.generator;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import core.common.CollectionHolder;
import core.common.ConstDataSource;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.common.DataSourceType;
import core.common.DbDataSource;
import core.common.ImgDataSource;
import core.common.VarHolder;
import core.common.XmlDataSource;
import core.generator.DataSourceConfigProcessor;

public class TestSetup implements DataSourceType {
	@Test
	public void readDataConfig() {
		DataSourceConfig dsc = DataSourceConfig.newInstance();
		DataSourceConfigProcessor dsch = new DataSourceConfigProcessor("/home/piao/piaoyong/UWR/00-客户提供资料/Template_new_datasource.xml");
		ImgDataSource ids = null;
		ConstDataSource cds = null;
		XmlDataSource xds = null;
		DbDataSource dds = null;
		
		dsch.parseConfigFile();
		
		assertTrue("数据源个数获取错误！", 4==dsc.getDataSources().size());
		
		cds = ((ConstDataSource)(dsc.getDataSources().get(0)));
		dds = ((DbDataSource)(dsc.getDataSources().get(1)));
		ids = ((ImgDataSource)(dsc.getDataSources().get(2)));
		xds = ((XmlDataSource)(dsc.getDataSources().get(3)));

		assertTrue("数据源0类型获取错误！", CONST.equalsIgnoreCase(cds.getType()));
		assertTrue("数据源1类型获取错误！", DB.equalsIgnoreCase(dds.getType()));
		assertTrue("数据源2类型获取错误！", IMG.equalsIgnoreCase(ids.getType()));
		
		assertTrue("数据库驱动获取错误","com.mysql.jdbc.Driver".equalsIgnoreCase(dds.getDriver()));
		assertTrue("文件路径获取错误！","d:/img1".equalsIgnoreCase(ids.getPath()));
		assertTrue("文件路径获取错误！","d:/xml1".equalsIgnoreCase(xds.getPath()));
		
		List<DataHolder> dhs = cds.getVars();
		assertTrue("常量数据获取错误！",dhs.size() == 10);
		assertTrue("常量值解析错误！","分析花费时间".equals(((VarHolder)dhs.get(7)).getValue()));
		
		CollectionHolder ch = (CollectionHolder)(dhs.get(9));
		assertTrue("MAP类型解析错误！","map".equals(dhs.get(9).getType()));
		assertTrue("MAP类型子元素解析错误！",7==ch.getVars().size());
		assertTrue("MAP类型子元素表达式解析错误！","${val1}".equals(ch.getVars().get(3).getExpr()));
	}

}
