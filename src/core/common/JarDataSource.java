package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * 常量数据源类
 * @author 朴勇 15641190702
 *
 */
public class JarDataSource extends DataSource {
    private String fullClassName;

    public JarDataSource(String name, String fullClassName) {
        super(name, JAR);
        this.setFullClassName(fullClassName);
        this.setVisitor(new HierarchyVisitor(this));
    }

    @Override
    public void cleanUp() {

    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }
}