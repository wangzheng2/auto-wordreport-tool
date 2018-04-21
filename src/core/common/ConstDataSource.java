package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * 常量数据源类
 * @author 王铮 18640548252
 * 
 */
public class ConstDataSource extends DataSource {

	public ConstDataSource() {
		super("", CONST);
		this.setVisitor(new SimpleVisitor(this));
	}

	@Override
	public void cleanUp() {

	}
}
