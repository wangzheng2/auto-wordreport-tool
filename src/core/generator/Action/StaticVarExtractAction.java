package core.generator.Action;

import org.apache.logging.log4j.Logger;

import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.VarHolder;
import core.generator.ReportGenerator;
import core.render.LiteralRender;

public class StaticVarExtractAction extends VarExtractAction {
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		int i = 0;
		String label = e.getMatch().group();
		Logger logger = ReportGenerator.getLogger();
		
		label = label.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:]*?var\\s*", "");
		label = label.replaceFirst("\\s*?/>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");
		logger.debug("***extract: " + label);
		
		String[] varinfo = label.split("#", 0); 
		
		DataSource ds = null;
		String dsname = null;
		//get DataSource name, could be null if it is a simple variable.
		for (i=0; i<varinfo.length; i++) {
			if (varinfo[i].matches("ds=\".*?\"")) {
				dsname = varinfo[i].toLowerCase().replaceFirst("ds=\"", "");
				dsname = dsname.replaceFirst("\"", "");
				logger.debug("dsname: " + dsname);
				//skip this <var ds=...>, we will process it further in step of label erasing
				logger.debug("SKIPPED!");
				return ReplaceAction.SKIP;
			}
		}
		ds = DataSourceConfig.newInstance().getDataSource(dsname);
		if (ds != null) {
			VarHolder vh = new VarHolder(ds, "", null, LiteralRender.newInstance());
			vh.setProcessloc(1);
			register(vh);
			
			for (i=0; i<varinfo.length; i++) {			
				if (varinfo[i].toLowerCase().matches("name=\".*?\"")) {
					String name = varinfo[i].toLowerCase().replaceFirst("name=\"", "");
					name = name.replaceFirst("\"", "");
					vh.setName(name);
					logger.debug("name: " + name);
				}
				if (varinfo[i].toLowerCase().matches("expr=\\\".*?\\\"")) {
					String expr = varinfo[i].replaceFirst("expr=\"", "");
					expr = expr.replaceFirst("\"", "");
					if (expr != null || "".equals(expr))
						vh.setExpr(expr);
					logger.debug("expr: " + expr);
					//skip this <var expr=...>, we will process it further in step of label erasing
					logger.debug("SKIPPED!");
					return ReplaceAction.SKIP;
				}
				if (varinfo[i].toLowerCase().matches("query=\\\".*?\\\"")) {
					String expr = varinfo[i].replaceFirst("query=\"", "");//not case sensitive!
					expr = expr.replaceFirst("\"", "");
					if (expr != null || "".equals(expr))
						vh.setExpr(expr);
					logger.debug("query: " + expr);
				}
				if (varinfo[i].toLowerCase().matches("value=\\\".*?\\\"")) {
					String value = varinfo[i].replaceFirst("value=\"", "");
					value = value.replaceFirst("\"", "");
					vh.setValue(value);
					logger.debug("value: " + value);
				}			
			}
			e.setReplacement("");
		}		
		return ReplaceAction.REPLACE;
	}
}
