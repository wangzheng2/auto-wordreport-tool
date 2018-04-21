package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * 图片数据源类
 * @author 王铮 18640548252
 * 
 */
public class ImgDataSource extends StreamDataSource{

	public ImgDataSource(String name, String path, boolean isFile) {
		super(name, IMG, path, isFile);
		setVisitor(new SimpleVisitor(this));
	}

	@Override
	public void cleanUp() {

	}
}
