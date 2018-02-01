package core.common;

public class ImgDataSource extends StreamDataSource{

	public ImgDataSource(String name, String path, boolean isFile) {
		super(name, IMG, path, isFile);
		//reuse the simple holder visitor, which is much more complicated than needed here
		//it is not necessary to recode another similar yet simpler one.
		setVisitor(new SimpleVisitor(this));
	}
}
