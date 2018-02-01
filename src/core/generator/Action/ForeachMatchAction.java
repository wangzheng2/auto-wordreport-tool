package core.generator.Action;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.logging.log4j.Logger;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.generator.ForeachLabel;
import core.generator.ReportGenerator;

import com.aspose.words.Node;

public class ForeachMatchAction implements IReplacingCallback {
	private ArrayList<ForeachLabel> foreaches = null;
	private LinkedList<ForeachLabel>feStack = new LinkedList<ForeachLabel>();
	private Logger logger = ReportGenerator.getLogger();
	
	public ForeachMatchAction(ArrayList<ForeachLabel> foreaches) {
		this.foreaches = foreaches;
	}
	
	private boolean isPostponed(ForeachLabel fel) {
		boolean b = false;
		for (ForeachLabel f: foreaches) {
			if(f.getFamily() == fel.getFamily().getParentNode()) {
				b = true;
				break;
			}
		}
		if (!fel.isInTable()) b = false;
		return b;
	}
	
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		ForeachLabel fel = null;
		String label = e.getMatch().group();
		Node node = e.getMatchNode();
		Node family = ForeachLabel.makeNormalFamily(node.getParentNode());
		
		label = label.replaceAll("”","\"");
		label = label.replaceAll("“","\"");
		label = label.replaceFirst("<[\\w:/]*?foreach\\s*", "");
		label = label.replaceFirst("\\s*>\\s*", "");
		label = label.replaceAll("\"\\s+", "\"#");
		
		if ("".equals(label)) { // end of <foreach> label
			fel = feStack.removeFirst();
			logger.debug("stack out: " + fel.getVarname());
			fel.setEnd(node);
			if (family != fel.getFamily()) logger.error("foreach_var: " + fel.getVarname() + " in a chaos status, please check!");	
			//here we only process one level each time, below levels will be ignored, which will be processed in following template scanning.
			if (feStack.size() <= 0) {
				//should we postpone this </foreach> erasing?
				if (isPostponed(fel)) {
					logger.debug("***postphone (foreach in TABLE): " + fel.getVarname());
					return ReplaceAction.SKIP;
				} else {
					foreaches.add(fel);
					fel.fillNodesInBetween();
				}			
				logger.debug("process this time: " + fel.getVarname());
			} else {
				logger.debug("***process NEXT time: " + fel.getVarname());
				return ReplaceAction.SKIP;
			}
		} else { // begin of <foreach> label
			fel = new ForeachLabel();
			String[] varinfo = label.split("#", 0); 		
			String varname = null;

			for (int i=0; i<varinfo.length; i++) {
				if (varinfo[i].matches("var=\".*?\"")) {
					varname = varinfo[i].toLowerCase().replaceFirst("var=\"", "");
					varname = varname.replaceFirst("\"", "");
					logger.info("foreach_var references: " + varname);
					break;
				}
			}
			
			if (!"".equals(varname)) {
				fel.setVarname(varname);
				fel.setBegin(node);
				fel.setFamily(node.getParentNode());
				logger.debug("stack in: " + varname);
				logger.debug("inTable: " + fel.isInTable());
				feStack.addFirst(fel);
			}
			
			//here we only process one level each time, below levels will be ignored, which will be processed in following template scanning.
			if (feStack.size() > 1) {
				return ReplaceAction.SKIP;
			}
			//should we postpone this <foreach> erasing?
			if (isPostponed(fel)) {
				return ReplaceAction.SKIP;
			} 
			
		};	
		return ReplaceAction.REPLACE;
	}
}
