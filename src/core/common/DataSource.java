package core.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一Word报告生成系统（UWR）
 * 抽象数据源类
 * @author 朴勇 15641190702
 * 
 */
public abstract class DataSource implements DataSourceType {
	//类型
	private String type = CONST;
	//数据源名字
	private String name = null;
	//挂载变量
	private List<DataHolder> vars = new ArrayList<DataHolder>();
	//数据访问器
	private HolderVisitor visitor = null;
	
	DataSource(String name, String type){
		setName(name);
		setType(type);
	}
	
	public String getType() {
		return type;
	}
	
	private void setType(String type) {
		if(CONST.equalsIgnoreCase(type.trim()))
			this.type = CONST;
		else if(DB.equalsIgnoreCase(type.trim()))
			this.type = DB;
		else if(XML.equalsIgnoreCase(type.trim()))
			this.type = XML;
		else if(JSON.equalsIgnoreCase(type.trim()))
			this.type = JSON;
		else if(IMG.equalsIgnoreCase(type.trim()))
			this.type = IMG;
		else if(JAR.equalsIgnoreCase(type.trim()))
			this.type = JAR;
		else
			this.type = NA;
	}
	
	public String getName() {
		return name;
	}
	
	private void setName(String name) {
		//名字不分大小写，程序内部全部转成小写
		this.name = name.trim().toLowerCase();
	}
	
	public List<DataHolder> getVars() {
		return vars;
	}

	public void setVars(ArrayList<DataHolder> vars) {
		if (vars != null)
			this.vars = vars;
	}
	
	public HolderVisitor getVisitor() {
		return visitor;
	}

	public void setVisitor(HolderVisitor hv) {
		this.visitor = hv;
	}
	
	//寻址
	public DataHolder getDataHolder(String name) {
		return getVisitor().getDataHolder(name);
	}

	public abstract  void cleanUp();
}
