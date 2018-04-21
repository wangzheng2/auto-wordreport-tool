package core.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一Word报告生成系统（UWR）
 * 链表类
 * @author 朴勇 15641190702
 * 
 */
public class ListHolder extends CollectionHolder {

	public ListHolder(DataSource ds, String name, ArrayList<DataHolder> vars) {
		super(ds, name, LIST, vars);
	}
	
	public ListHolder(DataSource ds, String name, List<DataHolder> vars, HolderRender render) {
		super(ds, name, LIST, vars);
		this.setHolderRender(render);
	}
}
