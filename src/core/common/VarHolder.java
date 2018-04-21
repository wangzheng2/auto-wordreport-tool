package core.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 统一Word报告生成系统（UWR）
 * 变量类
 * @author 王铮 18640548252
 * 
 */
public class VarHolder extends DataHolder {
	
	public VarHolder(DataSource ds, String name, String value) {
		super(ds, name, VALUE);
		this.setValue(value);
	}
	
	public VarHolder(DataSource ds, String name, String value, HolderRender render) {
		super(ds, name, VALUE);
		this.setValue(value);
		this.setHolderRender(render);
	}

	//返回大小
	@Override
	public int size() {
		Object value = this.getValue();
		if (value==null) return 0;
		if (value instanceof String) return 1;
		return ((CollectionHolder)value).size();
	}
	
	//按照名字返回个数，供回调
	@Override
	public long count(String attrname) {

		Map<String, Long> dataset = new HashMap<>();
		Long elem = null;
		if (attrname == null || "".equals(attrname))
			return super.count(attrname);
		Object value = this.getValue();
		if (value == null) return 0;
		if (value instanceof String) return 1;
		CollectionHolder lh = (CollectionHolder) value;

		Iterator<DataHolder> itr1 = lh.getVars().iterator();
		while (itr1.hasNext()) {
			CollectionHolder ch = (CollectionHolder) itr1.next();
			Iterator<DataHolder> itr2 = ch.getVars().iterator();
			while (itr2.hasNext()) {
				DataHolder dh = itr2.next();
				if (attrname.equalsIgnoreCase(dh.getName())) {
					Object val = dh.getValue();
					String tmp = null;
					if (val instanceof String) tmp = (String) val;
					dataset.put(tmp, elem);
				}
			}
		}
		this.setSwap(String.valueOf(dataset.size()));
		return dataset.size();
	}

	//按照名字返回和值，供回调。
	@Override
	public double sum(String attrname) {
		Object value = this.getValue();
		double dbsum = 0;
		
		if (value==null) return 0;
		if (value instanceof String) return Double.parseDouble((String)value);
		CollectionHolder lh = (CollectionHolder)value;
		
		for(int i=0; i<lh.size(); i++) {
			DataHolder dh = lh.getVars().get(i);
			dbsum += dh.sum(attrname);
		}
		this.setSwap(String.valueOf(dbsum));
		return dbsum;
	}

	@Override
	public double max(String attrname) {
		Object value = this.getValue();
		double dbmax = Double.MIN_VALUE;

		if (value==null) return 0;
		if (value instanceof String) return Double.parseDouble((String)value);
		CollectionHolder lh = (CollectionHolder)value;

		for(int i=0; i<lh.size(); i++) {
			DataHolder dh = lh.getVars().get(i);
			double dbtemp = dh.max(attrname);
			if(dbmax < dbtemp)
				dbmax = dbtemp;
		}
		this.setSwap(String.valueOf(dbmax));
		return dbmax;
	}

	@Override
	public double min(String attrname) {
		Object value = this.getValue();
		double dbmin = Double.MAX_VALUE;

		if (value==null) return 0;
		if (value instanceof String) return Double.parseDouble((String)value);
		CollectionHolder lh = (CollectionHolder)value;

		for(int i=0; i<lh.size(); i++) {
			DataHolder dh = lh.getVars().get(i);
			double dbtemp = dh.min(attrname);
			if(dbmin > dbtemp)
				dbmin = dbtemp;
		}
		this.setSwap(String.valueOf(dbmin));
		return dbmin;
	}

	//计数器，供回调
	public int inc(String attrname) {
		Object value = this.getValue();
		double dbvalue = 0;
		
		if (value==null) return 0;
		if (value instanceof String) {
			dbvalue = Double.parseDouble((String)value);
			dbvalue += 1;
			this.setValue(String.valueOf((int)dbvalue));
		}
		
		this.setSwap(String.valueOf(dbvalue));
		return (int)dbvalue;
	}

}
