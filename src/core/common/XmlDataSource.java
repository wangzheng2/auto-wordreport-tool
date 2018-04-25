package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * XML数据源类

 * 
 */
public class XmlDataSource extends StreamDataSource{

	public XmlDataSource(String name, String path, boolean isFile) {
		super(name, XML, path, isFile);
		setVisitor(new HierarchyVisitor(this));
	}

	@Override
	public void cleanUp() {

	}
}
