package core.generator.Action;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.generator.ForeachLabel;
import core.generator.ReportGenerator;

import com.aspose.words.Node;
import com.aspose.words.NodeType;

/**
 * 统一Word报告生成系统（UWR）
 * <foreach>标签的扫描与处理类

 * 
 */
public class ForeachMatchAction implements IReplacingCallback {
	private List<ForeachLabel> foreaches = null;
	private LinkedList<ForeachLabel>feStack = new LinkedList<ForeachLabel>();
	private Logger logger = ReportGenerator.getLogger();
	
	public ForeachMatchAction(List<ForeachLabel> foreaches) {
		this.foreaches = foreaches;
	}
	
	//判断是否需要延迟处理
	private boolean isPostponed(ForeachLabel fel) {
		boolean b = false;
		for (ForeachLabel f: foreaches) {
			if(f.getFamily() == fel.getFamily().getParentNode()) {
				b = true;
				break;
			}
		}
		//<fel>在cell中，延迟外包的<fel>。
		if (!fel.isInTable()) {
			if(fel.getFamily().getNodeType() == NodeType.CELL) {
				for (ForeachLabel f: foreaches) {
					if(f.getFamily() == fel.getFamily().getParentNode().getParentNode().getParentNode() || f.getFamily() == fel.getFamily().getParentNode().getParentNode()) {
						b = true;
						break;
					}
				}
			}			
			else b = false;
		}
		return b;
	}
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		ForeachLabel fel = null;
		String label = e.getMatch().group();
		Node node = e.getMatchNode();

		//匹配字符串的整理
		label = label.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:/]*?foreach\\s*", "");
		label = label.replaceFirst("\\s*>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");
		
		if ("".equals(label)) { //<foreach>的尾部
			fel = feStack.removeFirst();
			logger.debug("stack out: " + fel.getVarname());
			fel.setEnd(node);
			//只处理本层
			if (feStack.size() <= 0) {
				//是否需要延迟</foreach>的擦除？
				if (isPostponed(fel)) {
					logger.debug("***postphone (foreach in TABLE): " + fel.getVarname());
					return ReplaceAction.SKIP;
				} else {
					foreaches.add(fel);
					fel.fillNodesInBetween();
				}			
				logger.debug("process THIS run: " + fel.getVarname());
			} else {
				logger.debug("***process NEXT run: " + fel.getVarname());
				return ReplaceAction.SKIP;
			}
		} else { //<foreach>的头部
			fel = new ForeachLabel();
			String[] varinfo = label.split("#", 0); 		
			String varname = null, expand = null, defaultValue = null, separator=null;

			for (int i=0; i<varinfo.length; i++) {
				//获取var信息
				if (varinfo[i].matches("var=\".*?\"")) {
					varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
					varname = varname.replaceFirst("\"", "");
					logger.info("foreach_var references: " + varname);
				}
				//获取expand信息
				if (varinfo[i].matches("expand=\".*?\"")) {
					expand = varinfo[i].toLowerCase().replaceFirst("expand=\"", "");
					expand = expand.replaceFirst("\"", "");
					logger.info("expand: " + expand);
				}
				//获取default默认值信息
				if (varinfo[i].matches("default=\".*?\"")) {
					defaultValue = varinfo[i].toLowerCase().replaceFirst("default=\"", "");
					defaultValue = defaultValue.replaceFirst("\"", "");
					logger.info("Default Value: " + defaultValue);
				}
				//获取separators
				if (varinfo[i].matches("separator=\".*?\"")) {
					separator = varinfo[i].toLowerCase().replaceFirst("separator=\"", "");
					separator = separator.replaceFirst("\"", "");
					logger.info("Separator: " + separator);
				}
			}

			if (null != separator && !"".equals(separator)) {
				if(separator.matches("^\\|.*")) fel.setEndsep(separator.substring(1));
				else {
					String[] seps = separator.split("|");
					if (seps != null && seps.length > 0) {
						if (seps.length <= 2) fel.setMidsep(seps[0]);
						if (seps.length >= 3) {
							fel.setMidsep(seps[0]);
							fel.setEndsep(seps[2]);
						}
					}
				}
			}
			//如果需要按列展开
			if (!"".equals(varname)) {
				if ("column".equalsIgnoreCase(expand)) fel.setColumn(true);
				fel.setVarname(varname);
				fel.setBegin(node);
				fel.setDefaultValue(defaultValue);
				fel.setFamily(node.getParentNode());
				logger.debug("stack in: " + varname);
				logger.debug("inTable: " + fel.isInTable());
				feStack.addFirst(fel);
			}
			
			if (feStack.size() > 1) {
				return ReplaceAction.SKIP;
			}
			if (isPostponed(fel)) {
				return ReplaceAction.SKIP;
			} 
			
		};	
		return ReplaceAction.REPLACE;
	}
}
