package core.loader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

import org.apache.logging.log4j.Logger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import core.common.CollectionHolder;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.common.ListHolder;
import core.common.MapHolder;
import core.common.StreamDataSource;
import core.common.VarHolder;
import core.generator.ReportGenerator;
import core.render.LiteralRender;

/**
 * 缁熶竴Word鎶ュ憡鐢熸垚绯荤粺锛圲WR锛�
 * Json鏁版嵁鍔犺浇绫�
 * @author 鐜嬮摦
 * @author 鏈村媷 15641190702
 * 
 */
public class JsonLoader extends DataLoader {
	
	private static DataLoader jsonLoader = new JsonLoader();
	private Logger logger = ReportGenerator.getLogger();
	private static Map<String, Object> jsondocs = new HashMap<>();
	
	private JsonLoader() {};
	
	public static DataLoader newInstance() {
		return jsonLoader;
	}
	
	//鑾峰彇鏁版嵁
	@SuppressWarnings("unchecked")
	private String queryResult(DataHolder dh) throws IOException {
		
		if (dh == null) return String.valueOf(0);
		
		String[] params = dh.getExpr().split("%");
		String filename = null;
		String expr = null;
		if(params.length >= 2) {
			filename = params[0];
			expr = params[1];
		} else
			expr = params[0];
		
		String json = null;
		String fullpath = null;
		if (filename != null)
			fullpath = ((StreamDataSource) dh.getDataSource()).getPath() + File.separator + filename;
		else
			fullpath = ((StreamDataSource)dh.getDataSource()).getPath();

		logger.debug("json path: "+fullpath);

		Object document;
		document = jsondocs.get(fullpath);

		if (document == null) {
			json = new String(Files.readAllBytes(Paths.get(fullpath)), Charset.forName("GBK"));
			document = Configuration.defaultConfiguration().jsonProvider().parse(json);
			jsondocs.put(fullpath, document);
		}
		//鍙�冭檻2灞�
		List<Object> objs = JsonPath.read(document, expr);
		List<Map<String,String>> elems = new ArrayList<Map<String,String>>();

		Object obj = null;
		if(objs.size()<=0) {
			obj = new String("");
		} else {
			obj = objs.get(0);
		}

		if(obj instanceof String) {
			for(Object str:objs) {
				String strvalue = (String) str;
				String strkey = "value";
				Map<String, String> tmpres = new LinkedHashMap<String, String>();
				tmpres.put(strkey, strvalue);
				elems.add(tmpres);
			}
		} else if (obj instanceof List){
			elems = (List<Map<String, String>>) objs.get(0);
		} else {
			for(Object o:objs) {
				elems.add((Map<String, String>) o);
			}
		}
		
		List<DataHolder> nodedhs = new ArrayList<DataHolder>();
		Iterator<Map<String, String>> itr = elems.iterator();
		CollectionHolder ch = new ListHolder (dh.getDataSource(), "nodes", nodedhs, LiteralRender.newInstance());
		dh.setValue(ch);
		int j= 0;
		
		while (itr.hasNext()) {
			Map<String,String> valuemap = itr.next();
			List<DataHolder> attrdhs = new ArrayList<DataHolder>();
			DataHolder mapdh = new MapHolder(dh.getDataSource(), "item_" + j++, attrdhs, LiteralRender.newInstance());
			mapdh.setHolderRender(LiteralRender.newInstance());
			nodedhs.add(mapdh);
			//榛樿娣诲姞rawid灞炴��
			attrdhs.add(new VarHolder(dh.getDataSource(), "rawid", String.valueOf(j), LiteralRender.newInstance()));
			Set<String> keySet = valuemap.keySet();
			Iterator<String> itrkey = keySet.iterator();
			while(itrkey.hasNext()) {
				String key = itrkey.next();
				DataHolder vardh = new VarHolder(dh.getDataSource(), key , String.valueOf(valuemap.get(key)));
				vardh.setHolderRender(LiteralRender.newInstance());
				attrdhs.add(vardh);
			}
		}
		return String.valueOf(elems.size());
	}

	//濉厖
	@Override
	public String fill(DataHolder dh) throws Exception {
		String res = null;
		String expr = dh.getExpr();
		String oexpr = expr;
	
		CollectionHolder val = (CollectionHolder)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr) || val!=null) return String.valueOf(0);
			
		//鏄惁瀛樺湪鍙橀噺寮曠敤锛�
		logger.debug(expr);
		String tmpexpr = null;
		tmpexpr = expr;
		while(expr.matches(".*?\\$\\{.*")) {
			tmpexpr = expr.replaceFirst(".*?\\$\\{", "");
			tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
			DataHolder dhh = DataSourceConfig.newInstance().getDataHolder(tmpexpr);
			if ( dhh != null) {
				if (dhh.getValue() == null) dhh.fillValue();
				oexpr = oexpr.replaceAll(java.util.regex.Pattern.quote("${"+tmpexpr+"}"), Matcher.quoteReplacement(dhh.getValue().toString()));
			}
			expr=expr.replaceFirst("\\$\\{", "");
		}
		dh.setExpr(oexpr);
		res = queryResult(dh);
		logger.debug(res);
		return res;
	}

}
