package core.common;

/**
 * 统一Word报告生成系统（UWR）
 * 抽象访问者类
 * @author 朴勇 15641190702
 * 
 */
public abstract class HolderVisitor {
	public abstract DataHolder getDataHolder(String name);
}
