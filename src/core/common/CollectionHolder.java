package core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	@Override
	public int size() {
		if (vars != null) 
			return vars.size();
		else return 0;
	}
	
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
		return dataset.size();
	}
	
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
		return dbsum;	
	}
}
