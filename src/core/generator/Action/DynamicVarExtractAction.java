package core.generator.Action;

import java.util.ArrayList;
import org.apache.logging.log4j.Logger;

import com.aspose.words.Node;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.VarHolder;
import core.generator.ForeachLabel;
import core.generator.ReportGenerator;
import core.render.LiteralRender;

public class DynamicVarExtractAction extends VarExtractAction {
	private ArrayList<ForeachLabel> foreaches = null;
	private Logger logger = ReportGenerator.getLogger();
	
	public DynamicVarExtractAction(ArrayList<ForeachLabel> foreaches) {
		this.foreaches = foreaches;
	}
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		int i = 0;
		String label = e.getMatch().group();
		Node node = e.getMatchNode();
		Logger logger = ReportGenerator.getLogger();
		
		for (ForeachLabel fel:foreaches) {
			if(fel.isPart(node)) return ReplaceAction.SKIP;
		}

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
			}
		}
		ds = DataSourceConfig.newInstance().getDataSource(dsname);
		if (ds != null) {
			VarHolder vh = new VarHolder(ds, "", null, LiteralRender.newInstance());
			vh.setProcessloc(2);
			register(vh);
			
			for (i=0; i<varinfo.length; i++) {			
				if (varinfo[i].toLowerCase().matches("name=\".*?\"")) {
					String name = varinfo[i].toLowerCase().replaceFirst("name=\"", "");
					name = name.replaceFirst("\"", "");
					vh.setName(name);
					logger.debug("name: " + name);
				}
				if (varinfo[i].toLowerCase().matches("expr=\".*?\"")) {
					String expr = varinfo[i].toLowerCase().replaceFirst("expr=\"", "");
					expr = expr.replaceFirst("\"", "");
					if (expr != null || "".equals(expr))
						vh.setExpr(expr);
					logger.debug("expr: " + expr);
				}
				if (varinfo[i].toLowerCase().matches("query=.*")) {
					String expr = varinfo[i].replaceFirst("query=\"", "");//not case sensitive!
					expr = expr.replaceFirst("\"", "");
					if (expr != null || "".equals(expr))
						vh.setExpr(expr);
					logger.debug("query: " + expr);
				}
				if (varinfo[i].toLowerCase().matches("value=\".*?\"")) {
					String value = varinfo[i].toLowerCase().replaceFirst("value=\"", "");
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
