package core.generator.Action;

import org.apache.logging.log4j.Logger;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * 变量输出处理类
 * @author 张学龙
 * @author 朴勇 15641190702
 * 
 */
public class VarOutputAction implements IReplacingCallback {
	private Logger logger = ReportGenerator.getLogger();
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String label = e.getMatch().group();
		
		//匹配数据的整理
		label = label.replaceAll("”", "\"");
		label = label.replaceAll("“", "\"");
		label = label.replaceFirst("<[\\w:/]*?out\\s*", "");
		label = label.replaceFirst("\\s*[/]{0,1}>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");

		String[] varinfo = label.split("#", 0);

		String varname = null;

		for (int i = 0; i < varinfo.length; i++) {
			if (varinfo[i].matches("var=\".*?\"")) {
				//获取var信息
				varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
				varname = varname.replaceFirst("\"", "");
				varname = varname.replaceFirst("\\$\\{", "");
				varname = varname.replaceFirst("\\}", "");
				logger.debug("varname: " + varname);
				break;
			}
		}
		
		DataHolder dh = DataSourceConfig.newInstance().getDataHolder(varname);

		if (dh != null) {
			//呈现数据
			return dh.renderValue(e, varinfo);
		}
		
		if (ReportGenerator.isDebug)
			return ReplaceAction.SKIP;
		else {
			e.setReplacement("");
			return ReplaceAction.REPLACE;
		}
	}

}
