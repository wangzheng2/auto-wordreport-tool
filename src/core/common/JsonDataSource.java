package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * Json数据源类
 * @author 朴勇 15641190702
 * 
 */
public class JsonDataSource extends StreamDataSource{

	public JsonDataSource(String name, String path, boolean isFile) {
		super(name, JSON, path, isFile);
		setVisitor(new HierarchyVisitor(this));
	}

	@Override
	public void cleanUp() {

	}
}
