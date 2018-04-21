package core.generator.Action;

import com.aspose.words.IReplacingCallback;

import core.common.DataHolder;
import core.common.DataSource;
import core.common.DataSourceType;
import core.common.StreamDataSource;
import core.loader.*;
import core.render.ChartRender;
import core.render.ImgRender;

/**
 * 统一Word报告生成系统（UWR）
 * 变量抽取类
 * @author 张学龙
 * @author 朴勇 15641190702
 * 
 */
abstract class VarExtractAction implements IReplacingCallback, DataSourceType {
	
	protected void register(DataHolder dh) {
		DataSource ds = dh.getDataSource();
		ds.getVars().add(dh);
		
		if (CONST.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(ExprEvaluator.newInstance());
		else if (XML.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(XmlLoader.newInstance());
		else if (JAR.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(JarLoader.newInstance());
		else if (DB.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(DbLoader.newInstance());
		else if(IMG.equalsIgnoreCase(ds.getType())) {
			String path = ((StreamDataSource) ds).getPath();
			if ( path == null || "".equals(path)) {
				dh.setHolderFiller(ImgMaker.newInstance());
				dh.setHolderRender(new ChartRender());
			} else {
				dh.setHolderFiller(ImgLoader.newInstance());
				dh.setHolderRender(new ImgRender());
			}
		} else if(JSON.equalsIgnoreCase(ds.getType())) {
			dh.setHolderFiller(JsonLoader.newInstance());
		}
	}

}
