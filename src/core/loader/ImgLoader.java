package core.loader;

import core.common.DataHolder;
import core.common.StreamDataSource;
import core.generator.ReportGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Logger;

/**
 * 统一Word报告生成系统（UWR）
 * IMG数据加载器类（单例）
 * @author 王铮
 * @author 朴勇 15641190702
 * 
 */
public class ImgLoader extends DataLoader {
	
	private static final DataLoader imgLoader = new ImgLoader();
	private Logger logger = ReportGenerator.getLogger();
	
	private ImgLoader() {};
	
	public static DataLoader newInstance() {
		return imgLoader;
	}

	//填充
	@Override
	public String fill(DataHolder dh) throws Exception {
		String imgPath = null;
		BufferedImage img = null;
	
		Object val = dh.getValue();
		if (dh == null || val!=null) return String.valueOf(0);
		imgPath = ((StreamDataSource)dh.getDataSource()).getPath();
		img = ImageIO.read(new File(imgPath));
		if (img != null) dh.setValue(img);
		logger.debug("Image " + imgPath + " Loaded.");
		return "success";
	}

}
