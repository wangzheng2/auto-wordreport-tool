package core.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.aspose.words.Document;
import com.aspose.words.FindReplaceDirection;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.IReplacingCallback;
import com.aspose.words.Node;
import core.common.DataHolder;
import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.VarHolder;
import core.generator.Action.DynamicVarExtractAction;
import core.generator.Action.ForeachMatchAction;
import core.generator.Action.ForeachRewriteAction;
import core.generator.Action.OutRewriteAction;
import core.generator.Action.StaticVarExtractAction;
import core.generator.Action.VarOutputAction;
import core.generator.Action.VarRewriteAction;
import core.render.LiteralRender;

public class TemplateProcessor {
	private ArrayList<ForeachLabel> foreaches = new ArrayList<ForeachLabel>();
	Logger logger = ReportGenerator.getLogger();
	private int times = 0;

	public int getHasProcessed() {
		return foreaches.size();
	}
	
	private void singleForeachEraser(ForeachLabel fel, DataHolder dh, int seqno) throws Exception {
		FindReplaceOptions options = new FindReplaceOptions();
		Map<String, String> renamed = new LinkedHashMap<String,String>();
		IReplacingCallback varRewriteAction = new VarRewriteAction(fel, seqno, 0, renamed, dh);
		IReplacingCallback outRewriteAction = new OutRewriteAction(fel, 0, dh);

		String outpattern ="<[a-zA-Z:/]*?out\\s+[a-zA-Z]*?=.*?>" ;
		String varpattern="<[a-zA-Z]*?:{0,1}var.*?/>";
		
		//erase single <fel>
		for (int j = dh.size() - 1; dh != null && j >= 0; j--) {
			((OutRewriteAction) outRewriteAction).setIndex(j+1);
			((VarRewriteAction) varRewriteAction).setIndex(j+1);
			List<Node> inserted = new ArrayList<Node>();
			
			//let <fel> expand itself
			fel.expand(inserted);
			
			for (Node n : inserted) {
				//<var name=> rewrite
				options.ReplacingCallback = varRewriteAction;
				n.getRange().replace(Pattern.compile(varpattern), "", options);
				//<out var=> rewrite
				options.ReplacingCallback = outRewriteAction;
				n.getRange().replace(Pattern.compile(outpattern), "", options);
			}
			//here we handle var renames
			for (Node n : inserted) {			
				for(String varname : renamed.keySet()) {
					n.getRange().replace(Pattern.compile("[\"|”]"+varname+"[”|\"]"), "\""+renamed.get(varname)+"\"");
					n.getRange().replace(Pattern.compile("\\{"+varname+"\\}"), "{"+renamed.get(varname)+"}");
				}
			}
			renamed.clear();
		}
		fel.removeOrigin();
	}

	public String outVarReplace(String filename) throws Exception {
		String savedFile = null;
		Document doc = new Document(filename);

		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback varOutputAction = new VarOutputAction();
		options.ReplacingCallback = varOutputAction;
		doc.getRange().replace(Pattern.compile("<[a-zA-Z:/]*?out.*?>"), "", options);
		savedFile = filename.replaceAll("(.*)_[0-9]{1,1}(\\.[\\w]+)", "$1_3$2");
		doc.save(ReportGenerator.output);

		return savedFile;
	}

	public String foreachEraser(String filename) throws Exception {
		Document doc = new Document(filename);
		String savedFile = null;

		times++;
		foreaches.clear();
		FindReplaceOptions options = new FindReplaceOptions();		
		IReplacingCallback foreachMatchAction = new ForeachMatchAction(foreaches);
		IReplacingCallback dynamicVarExtractAction = new DynamicVarExtractAction(foreaches);
		options.setDirection(FindReplaceDirection.FORWARD);
		logger.info("----- <foreach> eraser: parsing ("+times+") -----");
		
		//discover what <foreach> labels should be processed in this erasing
		options.ReplacingCallback = foreachMatchAction;
		doc.getRange().replace(Pattern.compile("</{0,1}[a-zA-Z]*?:{0,1}foreach.*?>"), "", options);
		//dynamic load necessary variables
		options.ReplacingCallback = dynamicVarExtractAction;
		doc.getRange().replace(Pattern.compile("<[a-zA-Z]*?:{0,1}var.*?/>"), "", options);
		
		logger.info("----- <foreach> eraser: erasing ("+times+") -----");
		for (int i = foreaches.size() - 1; i >= 0; i--) { 
			// here in reverse order to erase <foreach> labels, that is from most outside into inside
			ForeachLabel fel = foreaches.get(i);
			logger.debug("expand foreach_var_ref: " + foreaches.get(i).getVarname());
			DataHolder dh = DataSourceConfig.newInstance().getDataHolder(fel.getVarname());
			if (dh != null) {
				if(dh instanceof VarHolder && dh.getValue() == null)
					dh.fillValue(); // fill this collection
				//expand this single <foreach>
				singleForeachEraser(fel, dh, i);
			}
		}
		savedFile = filename.replaceAll("(.*?)[_0-9]*(\\.[\\w]+)", "$1_2_"+times+"$2");
		doc.save(savedFile);
		return savedFile;
	}
	
	public  String foreachRewrite(String filename) throws Exception {
		Document doc = new Document(filename);
		String savedFile = null;

		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback foreachRewriteAction = new ForeachRewriteAction();
		options.ReplacingCallback = foreachRewriteAction;

		doc.getRange().replace(Pattern.compile("<[a-zA-Z:]*?foreach ds=.*?>"), "", options);
		savedFile = filename.replaceAll("(.*?)(\\.[\\w]+)", "$1_0$2");	
		doc.save(savedFile);
		
		return savedFile;
	}
	
	//VAR extractor
	public String staticVarExtract(String filename) throws Exception {
		
		String savedFile=null;
		DataSourceConfig dsc = DataSourceConfig.newInstance();
		DataSource ds = dsc.getConstDataSource();
		
		//before insert any template variables, a system dataholder is created.
		if (ds != null)
			ds.getVars().add(new VarHolder(ds, "system", "Universal Word Report Generator", LiteralRender.newInstance()));

		Document doc = new Document(filename);
		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback staticVarExtractAction = new StaticVarExtractAction();
		options.ReplacingCallback = staticVarExtractAction;

		doc.getRange().replace(Pattern.compile("<[a-zA-Z]*?:{0,1}var.*?/>"), "", options);	
		savedFile = filename.replaceAll("(.*)_[0-9]{1,1}(\\.[\\w]+)", "$1_1$2");	
		doc.save(savedFile);	
		
		return savedFile;
	}

}
