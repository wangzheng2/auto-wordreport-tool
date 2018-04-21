package core.render;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;

import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;
import core.common.DataHolder;
import core.common.HolderRender;
import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * 文字呈现器类（单例）
 * @author 陈安生
 * @author 朴勇 15641190702
 * 
 */
public class LiteralRender implements HolderRender {
	
	private static LiteralRender literalRender = new LiteralRender();
	private Logger logger = ReportGenerator.getLogger();
	
	private LiteralRender() {};
	
	public static LiteralRender newInstance() {
		return literalRender;
	}

	//呈现方法
	@Override
	public int render(DataHolder dh, ReplacingArgs e, String[] varinfo) throws Exception {
		
		String funcname = null, parmname = null, varvalue = null;
		Map<String, String> funcs = new LinkedHashMap<String, String>();
		//获取相关信息
		for (int i = 0; i < varinfo.length; i++) {
			if (varinfo[i].matches("func=\".*?\"")) {
				funcname = varinfo[i].replaceFirst("func=\"", ""); // case sensitive
				funcname = funcname.replaceFirst("\"", "");
				parmname = funcname.replaceFirst(".*?\\(", "");
				parmname = parmname.replaceFirst("\\)", "");
				funcname = funcname.replaceFirst("\\(.*", "");
				if (funcname.equals(parmname))
					parmname = "";// no parameters actually
				funcs.put(funcname, parmname);
				logger.debug("funcname: " + funcname);
				logger.debug("parmname: " + parmname);
			}
		}
		
		// 填充数据
		// 部分数据可能在<foreach>擦除阶段已经填充
		dh.fillValue();
		
		Set<String> keys = funcs.keySet();
		
		if (keys.isEmpty()) {
			varvalue = (String) dh.getValue();
		} else {
			Iterator<String> itr = keys.iterator();		
			while (itr.hasNext()) {
				funcname = itr.next();
				if("nolinebreak".equalsIgnoreCase(funcname))
					continue;
				parmname = funcs.get(funcname);
				Method method = dh.getClass().getMethod(funcname, new Class[] { String.class });
				method.invoke(dh, parmname);
				varvalue = (String)dh.getSwap();
			}
			dh.setSwap(null);
		}
		
		if (varvalue != null) {
			e.setReplacement(varvalue);
			return ReplaceAction.REPLACE;
		} else
			return ReplaceAction.SKIP;
	}
}
