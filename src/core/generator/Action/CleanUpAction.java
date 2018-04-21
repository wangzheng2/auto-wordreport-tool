package core.generator.Action;

import org.apache.logging.log4j.Logger;

import com.aspose.words.Cell;
import com.aspose.words.ControlChar;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.IReplacingCallback;
import com.aspose.words.Node;
import com.aspose.words.NodeType;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.generator.ReportGenerator;
/**
 * 统一Word报告生成系统（UWR）
 * 变量替换处理类
 *  @author 王铮 18640548252
 * 
 */
public class CleanUpAction implements IReplacingCallback {
	private Logger logger = ReportGenerator.getLogger();
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String label = e.getMatch().group();
		//匹配字符串的整理
		label = label.replaceAll("”", "\"");
		label = label.replaceAll("“", "\"");
		label = label.replaceFirst("<[\\w:/]*?out\\s*", "");
		label = label.replaceFirst("\\s*[/]{0,1}>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");

		String[] varinfo = label.split("#", 0);

		String varname = null, funcname=null, parmname=null;

		for (int i = 0; i < varinfo.length; i++) {
			//获取var信息
			if (varinfo[i].matches("var=\".*?\"")) {
				varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
				varname = varname.replaceFirst("\"", "");
				varname = varname.replaceFirst("\\$\\{", "");
				varname = varname.replaceFirst("\\}", "");
				logger.debug("varname: " + varname);
			}
			//获取func信息
			if (varinfo[i].matches("func=\".*?\"")) {
				funcname = varinfo[i].replaceFirst("func=\"", ""); // case sensitive
				funcname = funcname.replaceFirst("\"", "");
				parmname = funcname.replaceFirst(".*?\\(", "");
				parmname = parmname.replaceFirst("\\)", "");
				funcname = funcname.replaceFirst("\\(.*", "");
				if (funcname.equals(parmname))
					parmname = "";// no parameters actually
				//Cell中的合并处理
				if("nolinebreak".equalsIgnoreCase(funcname)) {
					Node node = e.getMatchNode();
					DocumentBuilder builder = new DocumentBuilder((Document) node.getDocument());
					while (node != null && node.getNodeType()!=NodeType.CELL)
						node = node.getParentNode();
					if (node != null && node.getNodeType()==NodeType.CELL) {
						Cell cell = (Cell) node;
						StringBuffer text = new StringBuffer();
						while (cell.getParagraphs().getCount() > 1) {
							String s = cell.getFirstParagraph().getText();
							s = s.replaceAll(ControlChar.CR,"");
							s = s.replaceAll(ControlChar.LINE_BREAK,"");
							s = s.replaceAll(ControlChar.CELL,"");
							text.append(s);
							cell.getFirstParagraph().remove();
						}
						cell.getLastParagraph().getChildNodes().clear();
						builder.moveTo(cell.getLastParagraph());
						builder.write(text.toString());
					}
					return ReplaceAction.SKIP;
				}
			}
		}
		
		if (ReportGenerator.isDebug)
			return ReplaceAction.SKIP;
		else {
			e.setReplacement("");
			return ReplaceAction.REPLACE;
		}
	}

}
