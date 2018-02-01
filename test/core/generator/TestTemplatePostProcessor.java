package core.generator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import core.common.DataSourceConfig;

public class TestTemplatePostProcessor {
	@Test
	public void testLabelRewrite() throws Exception {
		DataSourceConfig dsc = DataSourceConfig.newInstance();
		DataSourceConfigProcessor dsch = new DataSourceConfigProcessor("resource/Template_new_datasource.xml");
		dsch.parseConfigFile();
		TemplateProcessor tp = new TemplateProcessor();		
		tp.foreachRewrite("");
		tp.staticVarExtract("");
		
		tp.foreachEraser("");
		
		//ReportGenerator rg = new ReportGenerator();
		tp.outVarReplace("");
	}
	
	@Ignore
	//@Test
	public void testLogger() throws FileNotFoundException, IOException {
		Logger logger = ReportGenerator.getLogger();		
		logger.info("TEST PIAO");
		logger.debug("DEBUG PIAO");
		logger.error("ERROR PIAO");
	}

}
