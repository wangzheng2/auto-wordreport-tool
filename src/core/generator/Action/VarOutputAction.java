package core.generator.Action;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.DataSourceConfig;

public class VarOutputAction implements IReplacingCallback {
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String label = e.getMatch().group();
		label = label.replaceAll("”", "\"");
		label = label.replaceAll("“", "\"");
		label = label.replaceFirst("<[\\w:/]*?out\\s*", "");
		label = label.replaceFirst("\\s*[/]{0,1}>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");

		String[] varinfo = label.split("#", 0);

		String varname = null;

		//need only to find the varname
		//rest things should be handled by itself
		for (int i = 0; i < varinfo.length; i++) {
			if (varinfo[i].matches("var=\".*?\"")) {
				varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
				varname = varname.replaceFirst("\"", "");
				varname = varname.replaceFirst("\\$\\{", "");
				varname = varname.replaceFirst("\\}", "");
				System.out.println("varname: " + varname);
				break;
			}
		}
		
		DataHolder dh = DataSourceConfig.newInstance().getDataHolder(varname);

		if (dh != null) {
			return dh.renderValue(e, varinfo);
		}
		return ReplaceAction.SKIP;
	}

}
