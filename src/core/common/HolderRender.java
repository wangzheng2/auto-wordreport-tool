package core.common;

import com.aspose.words.ReplacingArgs;

/**
 * 统一Word报告生成系统（UWR）
 * 数据呈现接口
 * @author 朴勇 15641190702
 * 
 */
public interface HolderRender {
	public int render(DataHolder dh, ReplacingArgs e, String[] varinfo) throws Exception;

}
