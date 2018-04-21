package core.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一Word报告生成系统（UWR）
 * 映射类
 * @author 王铮 18640548252
 * 
 */
public class MapHolder extends CollectionHolder {

	public MapHolder(DataSource ds, String name, ArrayList<DataHolder> vars) {
		super(ds, name, MAP, vars);
	}
	
	public MapHolder(DataSource ds, String name, List<DataHolder> vars, HolderRender render) {
		super(ds, name, MAP, vars);
		this.setHolderRender(render);
	}

}
