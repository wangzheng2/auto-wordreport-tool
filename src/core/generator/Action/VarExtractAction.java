package core.generator.Action;

import com.aspose.words.IReplacingCallback;

import core.common.DataHolder;
import core.common.DataSource;
import core.common.DataSourceType;
import core.common.ImgDataSource;
import core.loader.DbLoader;
import core.loader.ExprEvaluator;
import core.loader.ImgMaker;
import core.loader.XmlLoader;
import core.render.ChartRender;

public abstract class VarExtractAction implements IReplacingCallback, DataSourceType {
	
	protected void register(DataHolder dh) {
		DataSource ds = dh.getDataSource();
		ds.getVars().add(dh);
		
		if (CONST.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(ExprEvaluator.newInstance());
		else if (XML.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(XmlLoader.newInstance());
		else if (DB.equalsIgnoreCase(ds.getType()))
			dh.setHolderFiller(DbLoader.newInstance());
		else if(IMG.equalsIgnoreCase(ds.getType())) {
			dh.setHolderFiller(ImgMaker.newInstance());
			String path = ((ImgDataSource)ds).getPath();
			if(path==null || "".equals(path))
				dh.setHolderRender(new ChartRender());
		}
		// other ds holder...
	}

}
