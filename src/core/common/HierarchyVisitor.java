package core.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一Word报告生成系统（UWR）
 * 层次访问者类
 * @author 朴勇 15641190702
 * 
 */
public class HierarchyVisitor extends SimpleVisitor{

	public HierarchyVisitor(DataSource ds) {
		super(ds);
	}
	
	//对XML数据源的变量提取方式设置为普通变量的提取方式
	//但这不支持层级节点的情况，因为普通变量没有层级的概念
	@Override
	public DataHolder getDataHolder(String name) {
		String[] levelname = null;
		VarHolder vh = null;
		CollectionHolder ch = null;
		List<DataHolder> range = null;
		
		if (name == null || "".equals(name)) return null;
		
		//tp[2].id => tp.nodes[2].id
		if (!name.matches("[\\w]+\\.nodes\\[[\\d]+\\]\\..*"))
			name = name.replaceAll("([a-zA-Z_]*?)\\[", "$1\\.nodes\\[");
		
		levelname = name.split("\\.", 0);
		
		if (levelname != null && levelname.length > 1) {
			vh = (VarHolder)getDataHolder(ds.getVars(), levelname[0]);
			if(vh!=null) {
				range = new ArrayList<DataHolder>();
				try {
					vh.fillValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
				range.add((CollectionHolder)(vh.getValue()));
			}
		} else {
			range = ds.getVars();
		}
		
		if (levelname != null && levelname.length > 2) {
			for (int i=1; i<levelname.length - 1; i++) {
				ch = (CollectionHolder)getDataHolder(range, levelname[i]);
				if (ch != null)
					range = ch.getVars();
			}
		}
		
		return getDataHolder(range, levelname[levelname.length - 1]);
	}

}
