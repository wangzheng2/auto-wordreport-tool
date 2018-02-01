package core.generator.Action;

import java.util.regex.Matcher;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.VarHolder;
import core.generator.ForeachLabel;

public class OutRewriteAction implements IReplacingCallback {
	
	private ForeachLabel fel = null;
	private int index = 0;
	private DataHolder dh = null;
	
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
		
		label = label.replaceAll("”","\"");
		olabel = olabel.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		olabel = olabel.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:/]*?out\\s*", "");
		label = label.replaceFirst("\\s*[/]{0,1}>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");
		
		String[] varinfo = label.split("#", 0); 
		
		String varname = null, scopename = null, funcname = null, parmname = null;

		//try to find varname in expression <s:out var= ... >, left labels will be ignored.
		for (int i=0; i<varinfo.length; i++) {
			if (varinfo[i].matches("var=\".*?\"")) {
				varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
				varname = varname.replaceFirst("\"", "");
				varname = varname.replaceFirst("\\$\\{", "");
				varname = varname.replaceFirst("\\}", "");
				// the same assume: last dot, original var reference
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				System.out.println("varname: " + varname);
				String[] dotnames = varname.split("\\.", 0);
				if (dotnames.length > 0)
					varname = dotnames[dotnames.length - 1];
				System.out.println("dotname: " + varname);
			}
			if (varinfo[i].matches("scope=\".*?\"")) {
				scopename = varinfo[i].toLowerCase().replaceFirst("scope=\"", ""); //case sensitive
				scopename = scopename.replaceFirst("\"", "");
				scopename = scopename.replaceFirst("\\(.*", "");
				System.out.println("scopename: " + scopename);
			}
			if (varinfo[i].matches("func=\".*?\"")) {
				funcname = varinfo[i].replaceFirst("func=\"", ""); //case sensitive
				funcname = funcname.replaceFirst("\"", "");
				parmname = funcname.replaceFirst(".*?\\(", "");
				parmname = parmname.replaceFirst("\\)", "");
				funcname = funcname.replaceFirst("\\(.*", "");
				if(funcname.equals(parmname)) parmname="";//no parameters actually
				System.out.println("funcname: " + funcname);
				System.out.println("parmname: " + parmname);
			}
		}
		
		String pattern = null;
		if (dh instanceof VarHolder) {
			pattern = fel.getVarname() + ".nodes[" + index + "]."+ varname ;
		} else if ("value".equals(varname)) {
			pattern = fel.getVarname() + "[" + index + "]";
		}
		
		if (scopename == null || "".equals(scopename) || fel.getVarname().equals(scopename))
			if(pattern != null && dh.getDataSource().getDataHolder(pattern) != null)				
				olabel = olabel.replaceAll("(<[\\w:/]*?out\\s*?var=\")(.*?)(\".*?>)", "$1"+Matcher.quoteReplacement("${" + pattern +"}")+"$3");
		else
			olabel = null;
		
		if("system".equals(varname)) olabel=null;
		
		if (olabel != null) {
			e.setReplacement(olabel);
			return ReplaceAction.REPLACE;
		} else {
			return ReplaceAction.SKIP;
		}
	}

}
