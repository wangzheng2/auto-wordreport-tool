package core.common;

import java.util.List;

public class SimpleVisitor extends HolderVisitor{
	DataSource ds = null;

	public SimpleVisitor(DataSource ds){
		this.ds = ds;
	}
	
	@Override
	public DataHolder getDataHolder(String name) {
		String[] levelname = null;
		CollectionHolder ch = null;
		List<DataHolder> range = ds.getVars();
		
		if (name == null || "".equals(name)) return null;
		levelname = name.split("\\.", 0);
		
		if (levelname != null && levelname.length > 1)		
			for (int i=0; i<levelname.length - 1; i++) {
				ch = (CollectionHolder)getDataHolder(range, levelname[i]);
				if (ch != null)
					range = ch.getVars();
			}
		
		return getDataHolder(range, levelname[levelname.length - 1]);
	}

	// name should NOT contain ANY dots ... ABC[2]
	// first find CollectionHolder ABC in dhs, then get the second DataHolder.
	protected DataHolder getDataHolder(List<DataHolder> dhs, String name) {
		DataHolder dh = null;
		String sindex = null;
		String sname = null;
		int index = -1;
		
		if (dhs == null || dhs.size() <= 0) return null;
		if (null == name || "".equals(name)) return null;
		// simple variable
		if (!name.matches(".*?\\[\\d+\\]")) {
			sname = name;
		} else { // list variable
			sname = name.replaceFirst("\\[\\d*\\].*", "");
			sindex = name.replaceFirst(".*?\\[.*?","");
			sindex = sindex.replaceFirst("\\].*","");
			index = Integer.parseInt(sindex);
		}
		
		for (int i = 0; i < dhs.size(); i++) {
			dh = dhs.get(i);
			if (sname.equalsIgnoreCase(dh.getName()))
				break;
			else
				dh = null;
		}
		
		if (dh == null) return null;
		if (index < 0) return dh;
		
		//here exception maybe thrown, should check!
		CollectionHolder ch = (CollectionHolder)dh;
		if (ch.getVars().size() < index || index<1) return null;
		return ch.getVars().get(index-1);
	}

}
