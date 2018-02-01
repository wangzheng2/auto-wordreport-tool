package core.render;

import java.lang.reflect.Method;

import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.HolderRender;

public class LiteralRender implements HolderRender {
	
	private static LiteralRender literalRender = new LiteralRender();
	
	private LiteralRender() {};
	
	public static LiteralRender newInstance() {
		return literalRender;
	}

	@Override
	public int render(DataHolder dh, ReplacingArgs e, String[] varinfo) throws Exception {
		
		String funcname = null, parmname = null, varvalue = null;
		// try to find varname in expression <s:out var= ... >, left labels will be
		// ignored.
		for (int i = 0; i < varinfo.length; i++) {
			if (varinfo[i].matches("func=\".*?\"")) {
				funcname = varinfo[i].replaceFirst("func=\"", ""); // case sensitive
				funcname = funcname.replaceFirst("\"", "");
				parmname = funcname.replaceFirst(".*?\\(", "");
				parmname = parmname.replaceFirst("\\)", "");
				funcname = funcname.replaceFirst("\\(.*", "");
				if (funcname.equals(parmname))
					parmname = "";// no parameters actually
				System.out.println("funcname: " + funcname);
				System.out.println("parmname: " + parmname);
			}
		}

		if (funcname != null) {
			Method method = dh.getClass().getMethod(funcname, new Class[] { String.class });
			Object obj = method.invoke(dh, parmname);
			varvalue = obj.toString();
		} else {
			// fill the dataholder, if already filled, then skip.
			// some collection vars are already filled in the phase of foreach eraser.
			dh.fillValue();
			varvalue = (String) dh.getValue();
		}
		
		if (varvalue != null) {
			e.setReplacement(varvalue);
			return ReplaceAction.REPLACE;
		} else {
			return ReplaceAction.SKIP;
		}
	}
}
