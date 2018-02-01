package core.common;

import java.util.ArrayList;
import java.util.Iterator;

public class DataSourceConfig implements DataSourceType {
	
	private static final DataSourceConfig dsc = new DataSourceConfig();
	private String filename = null;
	private ArrayList<DataSource> dataSources = null;
	
	private DataSourceConfig(){}
	
	public static DataSourceConfig newInstance() {
		return dsc;
	}
	
	public ArrayList<DataSource> getDataSources() {
		return dataSources;
	}
	
	public void setDataSources(ArrayList<DataSource> dss)  {
		this.dataSources = dss;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	//there should be only one ConstDataSource instance
	//so this method will return the first DataSource which is of ConstDataSource
	public ConstDataSource getConstDataSource() {
		if (dataSources==null) return null;
		for(int i=0; i<dataSources.size(); i++) {
			DataSource ds = dataSources.get(i);
			if ("ConstDataSource".equals(ds.getClass().getSimpleName()))
				return (ConstDataSource)ds;
		}
		return null;
	}
	//get desired datasource by name
	public DataSource getDataSource(String name) {
		if (name == null || "".equals(name)) return getConstDataSource();
		for(int i=0; i<dataSources.size(); i++) {
			DataSource ds = dataSources.get(i);
			if (name.equalsIgnoreCase(ds.getName())) return ds;
		}
		return null;
	}
	
	//address search sequence: DB->XML->Json->Const->Picture
	public DataHolder getDataHolder(String name) {
		DataHolder dh = null;	
		ArrayList<DataSource> constdss=new ArrayList<DataSource>();
		ArrayList<DataSource> dbdss=new ArrayList<DataSource>();
		ArrayList<DataSource> xmldss=new ArrayList<DataSource>();
		ArrayList<DataSource> jsondss=new ArrayList<DataSource>();
		ArrayList<DataSource> imgdss=new ArrayList<DataSource>();

		Iterator<DataSource> itr = this.getDataSources().iterator();
		while(itr.hasNext()) {
			DataSource ds = itr.next();
			if (CONST.equalsIgnoreCase(ds.getType())) 
				constdss.add(ds);
			else if (DB.equalsIgnoreCase(ds.getType()))
				dbdss.add(ds);
			else if (XML.equalsIgnoreCase(ds.getType()))
				xmldss.add(ds);
			else if (IMG.equalsIgnoreCase(ds.getType()))
				imgdss.add(ds);
			else if (JSON.equalsIgnoreCase(ds.getType()))
				jsondss.add(ds);
		}
		
		itr = dbdss.iterator();
		while(itr.hasNext()) {
			dh = itr.next().getDataHolder(name);
			if (dh != null) return dh;
		}
		
		itr = xmldss.iterator();
		while(itr.hasNext()) {
			dh = itr.next().getDataHolder(name);
			if (dh != null) return dh;
		}
		
		itr = jsondss.iterator();
		while(itr.hasNext()) {
			dh = itr.next().getDataHolder(name);
			if (dh != null) return dh;
		}
		
		itr = constdss.iterator();
		while(itr.hasNext()) {
			dh = itr.next().getDataHolder(name);
			if (dh != null) return dh;
		}
		
		itr = imgdss.iterator();
		while(itr.hasNext()) {
			dh = itr.next().getDataHolder(name);
			if (dh != null) return dh;
		}
		
		return null;
		
	}

}
