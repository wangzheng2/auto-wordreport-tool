package core.common;

public class ConstDataSource extends DataSource {

	public ConstDataSource() {
		super("", CONST);
		this.setVisitor(new SimpleVisitor(this));
	}
}
