package core.common;

public class XmlDataSource extends StreamDataSource{

	public XmlDataSource(String name, String path, boolean isFile) {
		super(name, XML, path, isFile);
		setVisitor(new HierarchyVisitor(this));
	}
}
