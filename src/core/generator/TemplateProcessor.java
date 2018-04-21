package core.generator;

import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import com.aspose.words.*;
import org.apache.logging.log4j.Logger;

import core.common.DataHolder;
import core.common.DataSource;
import core.common.DataSourceConfig;
import core.common.VarHolder;
import core.generator.Action.CleanUpAction;
import core.generator.Action.DynamicVarExtractAction;
import core.generator.Action.ForeachMatchAction;
import core.generator.Action.ForeachRewriteAction;
import core.generator.Action.OutRewriteAction;
import core.generator.Action.StaticVarExtractAction;
import core.generator.Action.VarOutputAction;
import core.generator.Action.VarRewriteAction;
import core.render.LiteralRender;

import static core.generator.ReportGenerator.*;

/**
 * 统一Word报告生成系统（UWR）
 * 模板引擎类
 * @author 张学龙
 * @author 朴勇 15641190702
 * 
 */
public class TemplateProcessor {
	private static Map<String, Document> docs = new HashMap<>();
	private List<ForeachLabel> foreaches = new ArrayList<ForeachLabel>();
	Logger logger = getLogger();
	private int times = 0;
	private String anyPrefix, outPattern, varPattern, foreachPattern, tmpFilePattern;

	TemplateProcessor(){
		if (null != prefix && !"".equals(prefix))
			this.anyPrefix = prefix + ":";
		else
			this.anyPrefix = "[a-zA-Z]*?:{0,1}";
		this.outPattern = "</{0,1}" + anyPrefix + "out\\s+[a-zA-Z]*?=.*?>";
		this.varPattern = "<" + anyPrefix + "var\\s+[a-zA-Z]*?=.*?/>";
		this.foreachPattern = "</{0,1}" + anyPrefix + "foreach.*?>";
		this.tmpFilePattern = "(.*?)[_0-9]*(\\.[\\w]+)";
	}

	public int getHasProcessed() {
		return foreaches.size();
	}
	
	//单一标签擦除器
	private void singleForeachEraser(ForeachLabel fel, DataHolder dh, int seqno) throws Exception {
		FindReplaceOptions options = new FindReplaceOptions();
		Map<String, String> renamed = new LinkedHashMap<String,String>();
		IReplacingCallback varRewriteAction = new VarRewriteAction(fel, seqno, 0, renamed, dh);
		IReplacingCallback outRewriteAction = new OutRewriteAction(fel, 0, dh);
		//擦除 single <fel>
		for (int j = dh.size() - 1; dh != null && j >= 0; j--) {
			((OutRewriteAction) outRewriteAction).setIndex(j+1);
			((VarRewriteAction) varRewriteAction).setIndex(j+1);
			List<Node> inserted = new ArrayList<Node>();
			//让其<fel>自己展开
			fel.expand(inserted);
			for (Node n : inserted) {
				//<var name=> 重写
				options.ReplacingCallback = varRewriteAction;
				n.getRange().replace(Pattern.compile(varPattern), "", options);
				//<out var=> 重写
				options.ReplacingCallback = outRewriteAction;
				n.getRange().replace(Pattern.compile(outPattern), "", options);
			}
			//重命名
			for (Node n : inserted) {
				for (String varname : renamed.keySet()) {
					n.getRange().replace(Pattern.compile("[\"|”]" + varname + "[”|\"]"), "\"" + renamed.get(varname) + "\"");
					n.getRange().replace(Pattern.compile("\\{" + varname + "\\}"), "{" + renamed.get(varname) + "}");
					n.getRange().replace(Pattern.compile("\\{" + varname + "\\["), "{" + renamed.get(varname) + "[");
					n.getRange().replace(Pattern.compile("\\{" + varname + "\\."), "{" + renamed.get(varname) + ".");
				}
			}
			//插入的节点
			Node n = inserted.get(0);
			if(null != n) {
				if(n.getNodeType() == NodeType.PARAGRAPH){
					Paragraph par = (Paragraph) n;
					RunCollection runs = par.getRuns();
					Run r = runs.get(runs.getCount()-1);
					if (r == null) continue;
					StringBuffer text = new StringBuffer();
					String s = r.getText().trim();
					text.append(s);
					if (j == dh.size()-1)
						text.append(fel.getEndsep());
					else
						text.append(fel.getMidsep());
					r.setText(text.toString());
				}
			}
			renamed.clear();
		}
		//默认值
		if (dh.size() <= 0 && !"".equals(fel.getDefaultValue()))
			fel.expandWithDefaultValue();
		else
			fel.removeOrigin();
	}
	//输出out标签的处理
	public String outVarReplace(String filename) throws Exception {
		String savedFile = null;
		Document doc;

		doc = docs.get(filename);

		if (doc == null){
            doc = new Document(filename);
            docs.put(filename, doc);
		}
		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback varOutputAction = new VarOutputAction();
		options.ReplacingCallback = varOutputAction;
		doc.getRange().replace(Pattern.compile(outPattern), "", options);
		savedFile = filename;
		//savedFile = filename.replaceAll(tmpFilePattern, "$1_3$2");
		//doc.save(savedFile);
		docs.put(savedFile, doc);
		return savedFile;
	}
	//生成终档
	public String cleanUpReplace(String filename) throws Exception {
		String savedFile = null;
		Document doc;
		doc = docs.get(filename);

		if (doc == null){
			doc = new Document(filename);
			docs.put(filename, doc);
		}
		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback cleanUpAction = new CleanUpAction();
		options.ReplacingCallback = cleanUpAction;
		doc.getRange().replace(Pattern.compile(outPattern), "", options);
		doc.updateFields();
		savedFile = output + "." + format;
		if ("pdf".equals(format))
			doc.save(savedFile, SaveFormat.PDF);
		else
			doc.save(savedFile, SaveFormat.DOC);
		return savedFile;
	}

	//标签foreach的擦除
	public String foreachEraser(String filename) throws Exception {
		Document doc;
		doc = docs.get(filename);

		if (doc == null){
			doc = new Document(filename);
			docs.put(filename, doc);
		}
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
		doc.getRange().replace(Pattern.compile(foreachPattern), "", options);
		//dynamic load necessary variables
		options.ReplacingCallback = dynamicVarExtractAction;
		doc.getRange().replace(Pattern.compile(varPattern), "", options);
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
		savedFile = filename;
		//savedFile = filename.replaceAll(tmpFilePattern, "$1_2_"+times+"$2");
		//doc.save(savedFile);
		docs.put(savedFile, doc);
		return savedFile;
	}
	
	//标签foreach重写
	public  String foreachRewrite(String filename) throws Exception {
		Document doc;
		doc = docs.get(filename);

		if (doc == null){
			doc = new Document(filename);
			docs.put(filename, doc);
		}
		String savedFile = null;
		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback foreachRewriteAction = new ForeachRewriteAction();
		options.ReplacingCallback = foreachRewriteAction;
		doc.getRange().replace(Pattern.compile(foreachPattern), "", options);
		savedFile = filename;
		//savedFile = filename.replaceAll(tmpFilePattern, "$1_0$2");
		//doc.save(savedFile);
		docs.put(savedFile, doc);
		return savedFile;
	}
	
	//VAR提取
	public String staticVarExtract(String filename) throws Exception {
		
		String savedFile=null;
		DataSourceConfig dsc = DataSourceConfig.newInstance();
		DataSource ds = dsc.getConstDataSource();
		//创建一个system变量
		if (ds != null)
			ds.getVars().add(new VarHolder(ds, "system", "Universal Word Report Generator", LiteralRender.newInstance()));
		Document doc;
		doc = docs.get(filename);

		if (doc == null){
			doc = new Document(filename);
			docs.put(filename, doc);
		}
		FindReplaceOptions options = new FindReplaceOptions();
		IReplacingCallback staticVarExtractAction = new StaticVarExtractAction();
		options.ReplacingCallback = staticVarExtractAction;
		doc.getRange().replace(Pattern.compile(varPattern), "", options);
		savedFile = filename;
		//savedFile = filename.replaceAll(tmpFilePattern, "$1_1$2");
		//doc.save(savedFile);
		docs.put(savedFile, doc);
		return savedFile;
	}
}
