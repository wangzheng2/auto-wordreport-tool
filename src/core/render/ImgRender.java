package core.render;

import java.awt.image.BufferedImage;

import com.aspose.words.*;
import org.apache.logging.log4j.Logger;

import core.common.DataHolder;
import core.common.HolderRender;
import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * Image呈现器类
 * @author 王铮 18640548252
 * 
 */
public class ImgRender implements HolderRender {
	private Logger logger = ReportGenerator.getLogger();

	//呈现方法
	@Override
	public int render(DataHolder dh, ReplacingArgs e, String[] varinfo) throws Exception {
		Node node = e.getMatchNode();
		DocumentBuilder builder = new DocumentBuilder((Document) node.getDocument());
		int height = 400, width = 600;
		int wraptype = WrapType.INLINE;
		
		if (dh == null) return ReplaceAction.SKIP;
		//按需填充
		if(dh.getValue() == null) dh.fillValue();
		
		String hei = null, wid = null, wrap = null;

		for (int i = 0; i < varinfo.length; i++) {
			//获取图形高度
			if (varinfo[i].matches("height=\".*?\"")) {
				hei = varinfo[i].toLowerCase().replaceFirst("height=\"", "");
				hei = hei.replaceFirst("\"", "");
				hei = hei.replaceFirst("\\$\\{", "");
				hei = hei.replaceFirst("\\}", "");
				logger.debug("height: " + hei);
				if (hei != null && !"".equals(hei)) {
					height = Integer.valueOf(hei);
				}
			}
			//获取图形长度
			if (varinfo[i].matches("width=\".*?\"")) {
				wid = varinfo[i].toLowerCase().replaceFirst("width=\"", "");
				wid = wid.replaceFirst("\"", "");
				wid = wid.replaceFirst("\\$\\{", "");
				wid = wid.replaceFirst("\\}", "");
				logger.debug("width: " + wid);
				if (wid != null && !"".equals(wid)) {
					width = Integer.valueOf(wid);
				}
			}
			//获取图形模式
			if (varinfo[i].matches("inline=\".*?\"")) {
				wrap = varinfo[i].toLowerCase().replaceFirst("inline=\"", "");
				wrap = wrap.replaceFirst("\"", "");
				wrap = wrap.replaceFirst("\\$\\{", "");
				wrap = wrap.replaceFirst("\\}", "");
				logger.debug("inline: " + wrap);
				if (wrap != null && !"".equals(wrap) && "off".equalsIgnoreCase(wrap)) {
					wraptype = WrapType.NONE;
				}
			}
		}
		
		BufferedImage bufferedImage = (BufferedImage) dh.getValue();
		
		if (bufferedImage == null) return ReplaceAction.SKIP;
		
		builder.moveTo(node);
		Shape shape = builder.insertImage(bufferedImage);
		shape.setWrapType(wraptype);
		shape.setBehindText(false);
		shape.setWidth(width);
		shape.setHeight(height);
		
		return ReplaceAction.REPLACE;
	}
}
