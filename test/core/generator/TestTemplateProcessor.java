package core.generator;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import core.common.ConstDataSource;
import core.common.DataHolder;
import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.DataSourceType;
import core.common.VarHolder;
import core.common.XmlDataSource;
import core.generator.DataSourceConfigProcessor;
import core.generator.TemplateProcessor;

public class TestTemplateProcessor implements DataSourceType{
	@Test
	public void testLabelRewrite() throws Exception {
		TemplateProcessor tp = new TemplateProcessor();
		tp.foreachRewrite("");
	}
	@Test
	public void testLabelExtract() throws Exception {
		DataSourceConfig dsc = DataSourceConfig.newInstance();
		DataSourceConfigProcessor dsch = new DataSourceConfigProcessor("src/resource/Template_new_datasource.xml");
		
		dsch.parseConfigFile();
		
		TemplateProcessor tp = new TemplateProcessor();
		tp.foreachRewrite("");
		tp.staticVarExtract("");
		
		DataSource ds = null;
		DataSource xmlds = null;
		
		assertTrue("数据源个数获取错误！", 4==dsc.getDataSources().size());	
		ds = ((ConstDataSource)(dsc.getDataSources().get(0)));
		xmlds = ((XmlDataSource)(dsc.getDataSources().get(3)));
		
		List<DataHolder> dhs = ds.getVars();
		assertTrue("模板常量数据获取错误！",dhs.size() == 17);
		assertTrue("模板解析Expr错误！","3+8".equals(((VarHolder)dhs.get(11)).getExpr()));
		DataHolder dh1 = ds.getDataHolder("tmp1");
		VarHolder dh2 = (VarHolder) xmlds.getDataHolder("tp1");
		dh1.fillValue();
		dh2.fillValue();
		
		DataHolder dh3 = ds.getDataHolder("tmp1");
		System.out.println("dh3: " + dh3.getValue());
		DataHolder dh4 = xmlds.getDataHolder("tp1.nodes[2].id");
		//DataHolder dh5 = xmlds.getDataHolder("tp1[1].id");
		System.out.println("dh4: " + dh4.getValue());
		System.out.println("dh2 size: " + dh2.size());
		//assertTrue("表达式解析错误！","12".equals(dhs.get(8).fillValue()));
		
		ds = dsc.getDataSource("db_mysql");
		if (ds != null) {
			assertTrue("模板数据库源变量定义获取错误！", ds.getVars().size() == 6);
			assertTrue("模板数据库源查询解析错误！", "select * from t_violate_count_stat".equals(ds.getVars().get(3).getExpr()));
		}
		
	}
}
