package core.generator.Action;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.aspose.words.Node;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.VarHolder;
import core.generator.ForeachLabel;
import core.generator.ReportGenerator;
import core.render.LiteralRender;
/**
 * 统一Word报告生成系统（UWR）
 * 动态变量处理类
 
 * 
 */
public class DynamicVarExtractAction extends VarExtractAction {
	private Logger logger = ReportGenerator.getLogger();
	private List<ForeachLabel> foreaches;
	
	public DynamicVarExtractAction(List<ForeachLabel> foreaches) {
		this.foreaches = foreaches;
	}
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		int i = 0;
		String label = e.getMatch().group();
		Node node = e.getMatchNode();
		
		for (ForeachLabel fel:foreaches) {
			if(fel.isPart(node)) return ReplaceAction.SKIP;
		}

		//匹配字符串的整理
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
			//获取数据源信息
			if (varinfo[i].matches("ds=\".*?\"")) {
				dsname = varinfo[i].toLowerCase().replaceFirst("ds=\"", "");
				dsname = dsname.replaceFirst("\"", "");
				logger.debug("dsname: " + dsname);
			}
		}
		
		String tmpds = null;
		//在数据源中寻址
		while(dsname != null && dsname.matches(".*?\\$\\{.*")) {
			tmpds = dsname.replaceFirst(".*?\\$\\{", "");
			tmpds = tmpds.replaceFirst("\\}.*", "");
			DataHolder dsdh = DataSourceConfig.newInstance().getDataHolder(tmpds);
			if ( dsdh != null) {
				//填充
				dsdh.fillValue();
				dsname=dsname.replaceFirst("\\$\\{.*?\\}", java.util.regex.Matcher.quoteReplacement((String) dsdh.getValue()));
			} else {
				logger.debug("Parameter in Datasource: " + tmpds +" can not be found! SKIPPED!");
				return ReplaceAction.SKIP;
			}
		}
		
		ds = DataSourceConfig.newInstance().getDataSource(dsname);
		if (ds != null) {
			VarHolder vh = new VarHolder(ds, "", null, LiteralRender.newInstance());
			vh.setProcessloc(2);
			//登记变量
			register(vh);
			
			for (i=0; i<varinfo.length; i++) {
				//获取name信息
				if (varinfo[i].toLowerCase().matches("name=\".*?\"")) {
					String name = varinfo[i].toLowerCase().replaceFirst("name=\"", "");
					name = name.replaceFirst("\"", "");
					vh.setName(name);
					logger.debug("name: " + name);
				}
				//获取expr信息
				if (varinfo[i].toLowerCase().matches("expr=\".*?\"")) {
					String expr = varinfo[i].toLowerCase().replaceFirst("expr=\"", "");
					expr = expr.replaceFirst("\"", "");
					if (expr != null || "".equals(expr))
						vh.setExpr(expr);
					logger.debug("expr: " + expr);
				}
				//获取query信息
				if (varinfo[i].toLowerCase().matches("query=.*")) {
					String expr = varinfo[i].replaceFirst("query=\"", "");//not case sensitive!
					expr = expr.replaceFirst("\"", "");
					if (expr != null || "".equals(expr))
						vh.setExpr(expr);
					logger.debug("query: " + expr);
				}
				//获取value信息
				if (varinfo[i].toLowerCase().matches("value=\".*?\"")) {
					String value = varinfo[i].toLowerCase().replaceFirst("value=\"", "");
					value = value.replaceFirst("\"", "");
					vh.setValue(value);
					logger.debug("value: " + value);
				}
			}
			//表达式参数

			if(vh.getName() == null || "".equals(vh.getName())){
				logger.error(e.getMatch().group() + " has no name and can not be referenced!");
				return ReplaceAction.SKIP;
			}

			String expr = vh.getExpr();
			String tmpexpr = null;
			//if all the parameters can be figured out
			while(expr != null && expr.matches(".*?\\$\\{.*")) {
				tmpexpr = expr.replaceFirst(".*?\\$\\{", "");
				tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
				DataHolder dhh = DataSourceConfig.newInstance().getDataHolder(tmpexpr);
				if ( dhh != null) {
					expr=expr.replaceFirst("\\$\\{", "");
				} else {
					logger.debug("Parameter " + tmpexpr +" can not be found! SKIPPED!");
					return ReplaceAction.SKIP;
				}
			}
			e.setReplacement("");
		} else {
			logger.error("Can not find datasource: " + dsname);
			return ReplaceAction.SKIP;
		}
		return ReplaceAction.REPLACE;
	}
}
