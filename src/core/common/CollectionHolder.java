package core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 统一Word报告生成系统（UWR）
 * 集合类
 * @author 王铮 18640548252
 * 
 */
public abstract class CollectionHolder extends DataHolder {

	private List<DataHolder> vars = new ArrayList<DataHolder>();
	
	CollectionHolder(DataSource ds, String name, String type, List<DataHolder> vars) {
		super(ds, name, type);
		setVars(vars);
	}

	public List<DataHolder> getVars() {
		return vars;
	}

	public void setVars(List<DataHolder> vars) {
		if (vars != null)
			this.vars = vars;
	}
	
	//返回该集合所含元素个数
	@Override
	public int size() {
		if (vars != null) 
			return vars.size();
		else return 0;
	}
	
	//按照变量名字返回个数，供回调。
	@Override
	public long count(String attrname) {
		Iterator<DataHolder> itr = vars.iterator();
		Map<String, Long> dataset = new HashMap<>();
		Long elem = null;
		if (attrname == null || "".equals(attrname)) return super.count(attrname);
		while (itr.hasNext()) {
			DataHolder dh = itr.next();
			if (attrname.equalsIgnoreCase(dh.getName())) {
				Object val = dh.getValue();
				String tmp = null;
				if (val instanceof String) tmp=(String)val;
				dataset.put(tmp, elem);
			}
		}
		this.setSwap(String.valueOf(dataset.size()));
		return dataset.size();
	}
	
	//按照变量名字返回和值，供回调。
	@Override
	public double sum(String attrname) {
		Iterator<DataHolder> itr = vars.iterator();
		double dbsum = 0;
		while(itr.hasNext()) {
			DataHolder dh = itr.next();
			if (attrname.equalsIgnoreCase(dh.getName())) {
				Object obj = dh.getValue();
				if (obj instanceof String)
					dbsum += Double.parseDouble((String)obj);
			}
		}
		this.setSwap(String.valueOf(dbsum));
		return dbsum;	
	}

	//按照变量名字返回最大值，供回调。
	@Override
	public double max(String attrname) {
		Iterator<DataHolder> itr = vars.iterator();
		double dbmax = Double.MIN_VALUE;
		while(itr.hasNext()) {
			DataHolder dh = itr.next();
			if (attrname.equalsIgnoreCase(dh.getName())) {
				Object obj = dh.getValue();
				if (obj instanceof String) {
					double dbtemp = Double.parseDouble((String)obj);
					if (dbmax < dbtemp)
						dbmax = dbtemp;
				}
			}
		}
		this.setSwap(String.valueOf(dbmax));
		return dbmax;
	}

	//按照变量名字返回最小值，供回调。
	@Override
	public double min(String attrname) {
		Iterator<DataHolder> itr = vars.iterator();
		double dbmin = Double.MAX_VALUE;
		while(itr.hasNext()) {
			DataHolder dh = itr.next();
			if (attrname.equalsIgnoreCase(dh.getName())) {
				Object obj = dh.getValue();
				if (obj instanceof String) {
					double dbtemp = Double.parseDouble((String)obj);
					if (dbmin > dbtemp)
						dbmin = dbtemp;
				}
			}
		}
		this.setSwap(String.valueOf(dbmin));
		return dbmin;
	}
}
