package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * 抽象流数据源类
 * @author 王铮 18640548252
 * 
 */
public abstract class StreamDataSource extends DataSource {
	//路径
	private String path = null;
	//是否为物理文件
	private boolean isFile = true;
	
	StreamDataSource(String name, String type, String path, boolean isFile) {
		super(name, type);
		this.path = path;
		this.isFile = isFile;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isFile() {
		return isFile;
	}

	
}
