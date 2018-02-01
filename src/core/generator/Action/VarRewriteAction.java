package core.generator.Action;

import java.util.Map;
import java.util.regex.Matcher;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.VarHolder;
import core.generator.ForeachLabel;

public class VarRewriteAction implements IReplacingCallback {
	
	private ForeachLabel fel = null;
	private int index = 0;
	private Map<String, String> renamed = null;
	private DataHolder dh = null;
	private int seqno = 0;
	
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
		
		label = label.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		olabel = olabel.replaceAll("”","\"");
		olabel = olabel.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:/]*?var\\s*", "");
		label = label.replaceFirst("\"\\s*/>\\s*", "\"");
		label = label.replaceAll("\"\\s+", "\"#");
		
		String[] varinfo = label.split("#", 0); 
		
		String varname = null, exprname = null;

		for (int i=0; i<varinfo.length; i++) {
			if (varinfo[i].matches("name=\".*?\"")) {
				varname = varinfo[i].toLowerCase().replaceFirst("name=\"", "");
				varname = varname.replaceFirst("\"", "");
				System.out.println("varname: " + varname);
			}
			
			//<var expr=.../> and <var query=.../> are exclusive.
			if (varinfo[i].matches("expr=\".*?\"")) {
				exprname = varinfo[i].replaceFirst("expr=\"", "");
				exprname = exprname.replaceFirst("\"", "");
				System.out.println("expr: " + exprname);
			}
			if (varinfo[i].matches("query=.*")) {
				exprname = varinfo[i].replaceFirst("query=\"", "");
				exprname = exprname.replaceFirst("\"", "");
				System.out.println("query: " + exprname);
			}
		}
		
		String rename = varname + "_" + seqno + index;
		String varpattern = "$1" + rename +"$3";		
		olabel = olabel.replaceAll("(<[\\w:/]*?var\\s*?name=\")(.*?)(\".*?>)", varpattern);
		
		if (exprname != null) {
			String tmpexpr = null;
			tmpexpr = exprname;
			while(exprname.matches(".*?\\$\\{.*")) {
				tmpexpr = exprname.replaceFirst(".*?\\$\\{", "");
				tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
				//assume the original parameter name in expr is after the last dot, which could have been rewritten. 
				// the same assume: last dot, original var reference
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				String[] oldname = tmpexpr.split("\\.",0);
				String pattern = null;
				if (dh instanceof VarHolder) {
					pattern = fel.getVarname() + ".nodes[" + index + "]."+ oldname[oldname.length - 1];
				} else if ("value".equals(tmpexpr)) {
					pattern = fel.getVarname() + "[" + index + "]";
				}
				//try to find if the referenced var is in the current datasource, rewrite it when needed.
				if (pattern != null && dh.getDataSource().getDataHolder(pattern) != null)
					olabel = olabel.replaceAll(java.util.regex.Pattern.quote("${"+tmpexpr+"}"), Matcher.quoteReplacement("${"+pattern+"}"));
				exprname=exprname.replaceFirst("\\$\\{", "");
			}
		}
		
		if (olabel != null) {
			renamed.put(varname.trim(), rename);
			e.setReplacement(olabel);
			return ReplaceAction.REPLACE;
		} else {
			return ReplaceAction.SKIP;
		}
	}

}
