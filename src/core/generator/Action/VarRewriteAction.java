package core.generator.Action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * 变量重写类
 * @author 张学龙
 * @author 朴勇 15641190702
 * 
 */
public class VarRewriteAction implements IReplacingCallback {
	//对应的<foreach>标签
	private ForeachLabel fel = null;
	private int index = 0;
	//需要重命名的记录
	private Map<String, String> renamed = null;
	//对应的数据来源
	private DataHolder dh = null;
	private int seqno = 0;
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

	public  VarRewriteAction(ForeachLabel fel, int seqno, int index, Map<String, String> renamed, DataHolder dh) {
		this.fel = fel;
		this.index  = index;
		this.renamed = renamed;
		this.dh =dh;
		this.seqno = seqno;
	}
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String label = e.getMatch().group();
		String olabel = label;
		
		//匹配字符串的整理
		label = label.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		olabel = olabel.replaceAll("”","\"");
		olabel = olabel.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:/]*?var\\s*", "");
		label = label.replaceFirst("\"\\s*/>\\s*", "\"");
		label = label.replaceAll("\"\\s+", "\"#");
		
		String[] varinfo = label.split("#", 0); 
		
		String varname = null, exprname = null;
		List<String> toProcessed = new ArrayList<String>();

		for (int i=0; i<varinfo.length; i++) {
			if (varinfo[i].matches("name=\".*?\"")) {
				//获取name信息
				varname = varinfo[i].toLowerCase().replaceFirst("name=\"", "");
				varname = varname.replaceFirst("\"", "");
				logger.debug("varname: " + varname);
			}
			
			//<var expr=.../>和<var query=.../>不会同时出现
			if (varinfo[i].matches("expr=\".*?\"")) {
				//获取expr信息
				exprname = varinfo[i].replaceFirst("expr=\"", "");
				exprname = exprname.replaceFirst("\"", "");
				toProcessed.add(exprname);
				logger.debug("expr: " + exprname);
			}
			if (varinfo[i].matches("query=.*")) {
				//获取query信息
				exprname = varinfo[i].replaceFirst("query=\"", "");
				exprname = exprname.replaceFirst("\"", "");
				toProcessed.add(exprname);
				logger.debug("query: " + exprname);
			}
			if (varinfo[i].matches("ds=.*")) {
				//获取ds信息
				exprname = varinfo[i].replaceFirst("ds=\"", "");
				exprname = exprname.replaceFirst("\"", "");
				toProcessed.add(exprname);
				logger.debug("ds: " + exprname);
			}
		}
		//重命名<var name=>当<fel>展开后
		String rename = varname + "_" + seqno + index;
		String varpattern = "$1" + rename +"$3";		
		olabel = olabel.replaceAll("(<[\\w:/]*?var\\s*?name=\")(.*?)(\".*?>)", varpattern);
		
		//更新变量的引用，如果必要的话。
		Iterator<String> itr = toProcessed.iterator();
		
		while(itr.hasNext()) {
			exprname = itr.next();
			if (exprname != null) {
				String tmpexpr = null;
				tmpexpr = exprname;
				while(exprname.matches(".*?\\$\\{.*")) {
					tmpexpr = exprname.replaceFirst(".*?\\$\\{", "");
					tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
					//假设变量名的出现位置处于最后一个dot之后（被重写后的）
					//相同的假设：last dot, original var reference
					String[] oldname = tmpexpr.split("\\.",0);
					String pattern = null;
					if (dh instanceof VarHolder) {
						pattern = fel.getVarname() + ".nodes[" + index + "]."+ oldname[oldname.length - 1];
					} else if ("value".equals(tmpexpr)) {
						pattern = fel.getVarname() + "[" + index + "]";
					}
					//引用的变量是否处于当前的数据源中，需要的话重写之。
					if (pattern != null && dh.getDataSource().getDataHolder(pattern) != null)
						olabel = olabel.replaceAll(java.util.regex.Pattern.quote("${"+tmpexpr+"}"), Matcher.quoteReplacement("${"+pattern+"}"));
					exprname=exprname.replaceFirst("\\$\\{", "");
				}
			}
		}
		
		if (olabel != null) {
			//记录重命名
			renamed.put(varname.trim(), rename);
			e.setReplacement(olabel);
			return ReplaceAction.REPLACE;
		} else {
			return ReplaceAction.SKIP;
		}
	}

}
