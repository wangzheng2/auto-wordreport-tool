package core.loader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DefaultPieDataset;
import core.common.CollectionHolder;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * 图形数据加载类（单例）
 *  @author 王铮 18640548252
 * 
 */
public class ImgMaker extends DataLoader{
	
	private static ImgMaker imgMaker = new ImgMaker();
	private Logger logger = ReportGenerator.getLogger();
	
	private ImgMaker() {};
	
	public static ImgMaker newInstance() {
		return imgMaker;
	}
	
	//填充
	@Override
	public String fill(DataHolder dh) throws Exception {
		
		String res = null;
		String expr = dh.getExpr();
		DataHolder dhvalue = null;
	
		CollectionHolder val = (CollectionHolder)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr) || val!=null) return String.valueOf(0);
			
		//parse expr to see if there are any variables contained
		logger.debug(expr);
		String[] parms = expr.split("\\|");
		int num = 5;
		if (parms == null || parms.length < 3) return "";
		if (parms[0] == null || "".equals(parms[0])) return "";
		if (parms[1] == null || "".equals(parms[1])) return "";
		if (parms[2] == null || "".equals(parms[2])) return "";
		if (parms.length >= 4 && (parms[3] != null || !"".equals(parms[3]))) {
			num = Integer.parseInt(parms[3]);
		}
		String tmpexpr = null;
		tmpexpr = parms[0];
		expr =tmpexpr;

		while(expr.matches(".*?\\$\\{.*")) {
			tmpexpr = expr.replaceFirst(".*?\\$\\{", "");
			tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
			dhvalue = DataSourceConfig.newInstance().getDataHolder(tmpexpr);
			if ( dhvalue != null) {
				dh.setExpr(expr);
				if (dhvalue.getValue() == null) dhvalue.fillValue();
			}
			expr=expr.replaceFirst("\\$\\{", "");
		}

		AbstractDataset dataset = null;

		if ("pie1".equalsIgnoreCase(parms[1])) {
            dataset = createPie1Dataset(dhvalue, parms[2]);
		} else if ("bar1".equalsIgnoreCase(parms[1])) {
			dataset = createBar1Dataset(dhvalue, parms[2], num);
		} else if ("bar2".equalsIgnoreCase(parms[1])) {
		dataset = createBar2Dataset(dhvalue, parms[2], num);
        }
		dh.setValue(dataset);
		res = "success";		
		return res;
	}

	//获取数据并转换至需要类型
	private AbstractDataset createBar1Dataset(DataHolder source, String category, int num) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Map<String, Integer> viospro5 = new TreeMap<>();
		CollectionHolder ch = (CollectionHolder) source.getValue();
		
		List<DataHolder> dhs = (List<DataHolder>) ch.getVars();
		for(int j = 0; j<dhs.size(); j++) {
			CollectionHolder cch = (CollectionHolder) dhs.get(j); //items
			List<DataHolder> dhss = cch.getVars(); //attributes of item
			for (int m = 1; m < dhss.size(); m++) {
				DataHolder dh = dhss.get(m);
				if (category.equalsIgnoreCase(dh.getName())) {
					String name = ((String)dh.getValue()).trim();
					Integer ii = viospro5.get(name);
					if(ii==null) ii=new Integer(0);
					ii = ii + ((CollectionHolder)cch.getValue()).getVars().size(); 
					viospro5.put(name, ii);
				}
			}
		}
		SortedSet<Entry<String, Integer>> viospro5final = entriesSortedByValues(viospro5);
		Iterator<Entry<String, Integer>> itr = viospro5final.iterator();
		int i = 0;
		while(itr.hasNext()) {
			i++;
			Entry<String, Integer> entry = itr.next();
			dataset.setValue(entry.getValue(), "dummy", entry.getKey());	
			if(i>=num) break;
		}
		return dataset;
	}
	
	//获取数据并转换至需要类型
	private AbstractDataset createBar2Dataset(DataHolder source, String category, int num) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Map<String, Integer> file5 = new TreeMap<>();
		CollectionHolder ch = (CollectionHolder) source.getValue();
		
		List<DataHolder> dhs = (List<DataHolder>) ch.getVars();
		for (int j = 0; j < dhs.size(); j++) {
			CollectionHolder cch = (CollectionHolder) dhs.get(j); // items
			List<DataHolder> dhss = ((CollectionHolder) cch.getValue()).getVars(); // attributes of violation
			for (int k = 0; k < dhss.size(); k++) {
				List<DataHolder> dhsss = ((CollectionHolder) dhss.get(k)).getVars();
				for (int m = 0; m < dhsss.size(); m++) {
					DataHolder dh = dhsss.get(m);
					if (category.equalsIgnoreCase(dh.getName())) {
						String file = ((String) dh.getValue()).trim().replaceAll(".*\\\\", "");
						Integer ii = file5.get(file);
						if (ii == null) ii = new Integer(0);
						ii++;
						file5.put(file, ii);
					}
				}
			}
		}
		SortedSet<Entry<String, Integer>> file5final = entriesSortedByValues(file5);
		Iterator<Entry<String, Integer>> itr = file5final.iterator();
		int i = 0;
		while(itr.hasNext()) {
			i++;
			Entry<String, Integer> entry = itr.next();
			dataset.setValue(entry.getValue(), "dummy", entry.getKey());	
			if(i>=num) break;
			
		}	
		return dataset;
	}

	//获取数据并转换至需要类型
	private AbstractDataset createPie1Dataset(DataHolder source, String category) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		Map<String, Integer> prioritymap = new HashMap<>();	
		CollectionHolder ch = (CollectionHolder)source.getValue();

		List<DataHolder> dhs = (List<DataHolder>) ch.getVars();
		for(int j = 0; j<dhs.size(); j++) {
			CollectionHolder cch = (CollectionHolder) dhs.get(j); //items节点
			List<DataHolder> dhss = cch.getVars(); //item的属性
			for (int m = 0; m < dhss.size(); m++) { 
				DataHolder dh = dhss.get(m);
				if (category.equalsIgnoreCase(dh.getName())) {
					String name = ((String)dh.getValue()).trim();
					Integer ii = prioritymap.get(name);
					if(ii==null) ii=new Integer(0);
					ii = ii + ((CollectionHolder)cch.getValue()).getVars().size(); 
					prioritymap.put(name, ii);
				}
			}
		}
		
		Set<String> keys = prioritymap.keySet();
		Iterator<String> itr = keys.iterator();
		
		while(itr.hasNext()) {
			String k = itr.next().trim();
			dataset.setValue(k, prioritymap.get(k));	
		}

		return dataset;
	}
	
	private <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>( 
	    		new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e2.getValue().compareTo(e1.getValue());
	                return res != 0 ? res : 1; //顺序：reverse order
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
}
