package core.common;

public abstract class StreamDataSource extends DataSource {

	private String path = null;
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
