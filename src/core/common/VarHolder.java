package core.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

	@Override
	public int size() {
		Object value = this.getValue();
		if (value==null) return 0;
		if (value instanceof String) return 1;
		return ((CollectionHolder)value).size();
	}
	
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
		return dataset.size();
	}

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
		return dbsum;
	}

}
