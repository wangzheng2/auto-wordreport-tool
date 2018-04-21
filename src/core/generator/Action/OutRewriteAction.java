package core.generator.Action;

import java.util.regex.Matcher;

import org.apache.logging.log4j.Logger;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.VarHolder;
import core.generator.ForeachLabel;
import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * <out>标签重写类
 * @author 张学龙
 * @author 朴勇 15641190702
 * 
 */
public class OutRewriteAction implements IReplacingCallback {
	//对应的<foreach>标签
	private ForeachLabel fel = null;
	private int index = 0;
	private DataHolder dh = null;
	private Logger logger = ReportGenerator.getLogger();
	
	public ForeachLabel getFel() {
		return fel;
	}

	public void setFel(ForeachLabel fel) {
		this.fel = fel;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public  OutRewriteAction(ForeachLabel fel, int index, DataHolder dh) {
		this.fel = fel;
		this.index  = index;
		this.dh = dh;
	}
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String label = e.getMatch().group();
		String olabel = label;
		
		//匹配字符串的整理
		label = label.replaceAll("”","\"");
		olabel = olabel.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		olabel = olabel.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:/]*?out\\s*", "");
		label = label.replaceFirst("\\s*[/]{0,1}>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");
		
		String[] varinfo = label.split("#", 0); 
		
		String varname = null, scopename = null, funcname = null, parmname = null, originname = null;

		//try to find varname in expression <s:out var= ... >, left labels will be ignored.
		for (int i=0; i<varinfo.length; i++) {
			if (varinfo[i].matches("var=\".*?\"")) {
				//获取var信息
				varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
				varname = varname.replaceFirst("\"", "");
				varname = varname.replaceFirst("\\$\\{", "");
				varname = varname.replaceFirst("\\}", "");
				// the same assume: last dot, original var reference
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				logger.debug("varname: " + varname);

			}
			if (varinfo[i].matches("scope=\".*?\"")) {
				//获取scope信息
				scopename = varinfo[i].toLowerCase().replaceFirst("scope=\"", ""); //case sensitive
				scopename = scopename.replaceFirst("\"", "");
				scopename = scopename.replaceFirst("\\(.*", "");
				logger.debug("scopename: " + scopename);
			}
			if (varinfo[i].matches("origin=\".*?\"")) {
				//获取origin信息
				originname = varinfo[i].toLowerCase().replaceFirst("origin=\"", ""); //case sensitive
				originname = originname.replaceFirst("\"", "");
				originname = originname.replaceFirst("\\(.*", "");
				logger.debug("origin: " + originname);
			}
			if (varinfo[i].matches("func=\".*?\"")) {
				//获取func信息
				funcname = varinfo[i].replaceFirst("func=\"", ""); //case sensitive
				funcname = funcname.replaceFirst("\"", "");
				parmname = funcname.replaceFirst(".*?\\(", "");
				parmname = parmname.replaceFirst("\\)", "");
				funcname = funcname.replaceFirst("\\(.*", "");
				if(funcname.equals(parmname)) parmname="";//no parameters actually
				logger.debug("funcname: " + funcname);
				logger.debug("parmname: " + parmname);
			}
		}

		String pattern = null;
		boolean withOrigin = true, withDotName = true;
        if (originname == null || "".equals(originname)) {
        	withOrigin = false;
        	originname = varname;
		}

		String [] dotnames = null;
		dotnames = originname.split("\\.", 0);
		if (dotnames.length <= 1) {
			withDotName = false;
		}

		if (dh instanceof VarHolder && !withDotName) {
			pattern = fel.getVarname() + ".nodes[" + index + "]."+originname;
		} else if ("value".equals(varname)) {
			pattern = fel.getVarname() + "[" + index + "]";
		}
		//对scope指定为本级的进行处理变量寻址
		logger.debug("pattern: " + pattern);
		if ((scopename == null || "".equals(scopename) || fel.getVarname().equals(scopename)))
			if(pattern != null && dh.getDataSource().getDataHolder(pattern) != null){
				if (!withOrigin) {
					if (!withDotName)
						olabel = olabel.replaceAll("(<[\\w:/]*?out\\s*?var=\")(.*?)(\")(.*?>)",
								"$1" + Matcher.quoteReplacement("${" + pattern + "}") + "$3 origin=\"" + originname + "\"$4");
					else
						olabel = olabel.replaceAll("(<[\\w:/]*?out\\s*?var=\")(.*?)(\")(.*?>)",
								"$1$2$3 origin=\"" + originname + "\"$4");
				} else { //origin is not empty
					if (!withDotName)
						olabel = olabel.replaceAll("(<[\\w:/]*?out\\s*?var=\")(.*?)(\")(.*?>)",
								"$1" + Matcher.quoteReplacement("${" + pattern + "}") + "$3$4");
					else
						olabel = null;
				}
			}
		else
			olabel = null;
		
		//system不处理
		if("system".equals(varname)) olabel=null;
		
		if (olabel != null) {
			e.setReplacement(olabel);
			return ReplaceAction.REPLACE;
		} else {
			return ReplaceAction.SKIP;
		}
	}

}
