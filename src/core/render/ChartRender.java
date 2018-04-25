package core.render;

import com.aspose.words.*;
import core.common.DataHolder;
import core.common.HolderRender;
import core.generator.ReportGenerator;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Rotation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

/**
 * 统一Word报告生成系统（UWR）
 * Chart呈现器类

 * 
 */
public class ChartRender implements HolderRender {
	private Logger logger = ReportGenerator.getLogger();

	//呈现方法
	@Override
	public int render(DataHolder dh, ReplacingArgs e, String[] varinfo) throws Exception {
		Node node = e.getMatchNode();
		DocumentBuilder builder = new DocumentBuilder((Document) node.getDocument());
		int height = 400, width = 600;
		String title = "Default Title", axisX = "Default AxisX", axisY = "Default AxisY";
		
		if (dh == null) return ReplaceAction.SKIP;
		//按需填充
		if(dh.getValue() == null) dh.fillValue();
		
		String chart = null, hei = null, wid = null;
		//获取图形类型
		for (int i = 0; i < varinfo.length; i++) {
			if (varinfo[i].matches("chart=\".*?\"")) {
				chart = varinfo[i].toLowerCase().replaceFirst("chart=\"", "");
				chart = chart.replaceFirst("\"", "");
				logger.debug("chart: " + chart);
			}
			//获取图形高度
			if (varinfo[i].matches("height=\".*?\"")) {
				hei = varinfo[i].toLowerCase().replaceFirst("height=\"", "");
				hei = hei.replaceFirst("\"", "");
				logger.debug("height: " + hei);
				if (hei != null && !"".equals(hei)) {
					height = Integer.valueOf(hei);
				}
			}
			//获取图形长度
			if (varinfo[i].matches("width=\".*?\"")) {
				wid = varinfo[i].toLowerCase().replaceFirst("width=\"", "");
				wid = wid.replaceFirst("\"", "");
				logger.debug("width: " + wid);
				if (wid != null && !"".equals(wid)) {
					width = Integer.valueOf(wid);
				}
			}
			//获取图形标题
			if (varinfo[i].matches("title=\".*?\"")) {
				title = varinfo[i].toLowerCase().replaceFirst("title=\"", "");
				title = title.replaceFirst("\"", "");
				logger.debug("title: " + title);
			}
			//获取图形X轴标题
			if (varinfo[i].matches("axisx=\".*?\"")) {
				axisX = varinfo[i].toLowerCase().replaceFirst("axisx=\"", "");
				axisX = axisX.replaceFirst("\"", "");
				logger.debug("axisX: " + axisX);
			}
			//获取图形Y轴标题
			if (varinfo[i].matches("axisy=\".*?\"")) {
				axisY = varinfo[i].toLowerCase().replaceFirst("axisy=\"", "");
				axisY = axisY.replaceFirst("\"", "");
				logger.debug("axisY: " + axisY);
			}
		}
		
		if (chart == null || "".equals(chart)) chart="pie";
		if(dh.getValue() == null) return ReplaceAction.SKIP;
		JFreeChart fchart = null;
		if ("bar".equals(chart)) {
			fchart = makeBarChart(dh.getValue(), title, axisX, axisY);
		}
		else if ("line".equals(chart)) {
			fchart = makeLineChart(dh.getValue(), title, axisX, axisY);
		}
		else if ("pie".equals(chart)) {
			fchart = makePieChart(dh.getValue(), title);
		}
		if (fchart == null) return ReplaceAction.SKIP;	
		updateJFreeChartBeforeExport(fchart);
		
		BufferedImage bufferedImage = fchart.createBufferedImage(width, height);
		builder.moveTo(node);
		builder.insertImage(bufferedImage);
		return ReplaceAction.REPLACE;
	}
	
	//按照数据生成图形
	public JFreeChart makePieChart(Object ds, String title) {
		StandardChartTheme theme = new StandardChartTheme("CN");
        theme.setExtraLargeFont(new Font("宋体", Font.BOLD, 12)); // 设置标题字体
        theme.setLargeFont(new Font("宋体", Font.BOLD, 12));
        theme.setRegularFont(new Font("宋体", Font.BOLD, 12));
        ChartFactory.setChartTheme(theme);

		JFreeChart chart = ChartFactory.createPieChart3D(title, (PieDataset) ds, true, false, false);
		chart.setBackgroundPaint(SystemColor.window);
		PiePlot3D plot3d = (PiePlot3D) chart.getPlot();
		plot3d.setStartAngle(270.0D); // 起始角度
		plot3d.setBackgroundPaint(SystemColor.window);
		plot3d.setDirection(Rotation.ANTICLOCKWISE); // 逆时针
		plot3d.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", NumberFormat.getNumberInstance(),
				NumberFormat.getPercentInstance()));
		// 去边框
		plot3d.setOutlinePaint(SystemColor.window); // 设置绘图面板外边的填充颜色
		plot3d.setShadowPaint(SystemColor.window); // 设置绘图面板阴影的填充颜色
		// 设置图例位置
		LegendTitle legend = chart.getLegend();
		legend.setPosition(RectangleEdge.RIGHT);
		legend.setBackgroundPaint(SystemColor.window);
		legend.setHorizontalAlignment(HorizontalAlignment.LEFT);
		legend.setMargin(5, 5, 5, 5);
		legend.setFrame(new BlockBorder(0, 0, 0, 0)); // 全部设为0表示无border
		legend.setLegendItemGraphicPadding(new RectangleInsets(5, 5, 5, 5));

		return chart;
	}
	
	public JFreeChart makeBarChart(Object ds, String title, String axisX, String axisY) {
		StandardChartTheme theme = new StandardChartTheme("CN");
		theme.setExtraLargeFont(new Font("宋体", Font.BOLD, 12)); // 设置标题字体
		theme.setLargeFont(new Font("宋体", Font.BOLD, 12));
		theme.setRegularFont(new Font("宋体", Font.BOLD, 12));
		ChartFactory.setChartTheme(theme);

		JFreeChart chart = ChartFactory.createBarChart(title, axisX, axisY, (CategoryDataset) ds, PlotOrientation.VERTICAL, false, false, false);

		chart.setBorderPaint(SystemColor.window);
        chart.setBackgroundPaint(SystemColor.window);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(SystemColor.window);
        plot.setDomainGridlinePaint(SystemColor.window);
        CustomBarRenderer3D renderer3d = new CustomBarRenderer3D();
        renderer3d.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer3d.setDefaultItemLabelsVisible(true);
        renderer3d.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER));
        plot.setRenderer(renderer3d);
        renderer3d.setMaximumBarWidth(0.05D);
        renderer3d.setDefaultPaint(SystemColor.window); // 设置墙体颜色
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setMaximumCategoryLabelLines(Integer.MAX_VALUE);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

		return chart;
	}

	public JFreeChart makeLineChart(Object ds, String title, String axisX, String axisY) {
		StandardChartTheme theme = new StandardChartTheme("CN");
		theme.setExtraLargeFont(new Font("宋体", Font.BOLD, 12)); // 设置标题字体
		theme.setLargeFont(new Font("宋体", Font.BOLD, 12));
		theme.setRegularFont(new Font("宋体", Font.BOLD, 12));
		ChartFactory.setChartTheme(theme);

		JFreeChart chart = ChartFactory.createLineChart(title, axisX, axisY, (CategoryDataset) ds, PlotOrientation.VERTICAL, false, false, false);

		chart.setBorderPaint(SystemColor.window);
		chart.setBackgroundPaint(SystemColor.window);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(SystemColor.window);
		plot.setDomainGridlinePaint(SystemColor.window);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setMaximumCategoryLabelLines(Integer.MAX_VALUE);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

		return chart;
	}

	public void updateJFreeChartBeforeExport(JFreeChart chart) {
        chart.setBackgroundPaint(Color.white);
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setBackgroundPaint(Color.white);
        }
        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.white);
        if (plot instanceof CategoryPlot) {
            CategoryItemRenderer renderer = ((CategoryPlot) plot).getRenderer();
            if (renderer instanceof BarRenderer) {
                ((BarRenderer) renderer).setDefaultPaint(Color.white);
            }
        }
	}
	
	public class CustomBarRenderer3D extends BarRenderer {
		private static final long serialVersionUID = 1L;

		public Paint getItemPaint(int paramInt1, int paramInt2) {
			switch (paramInt2) {
			case 0:
				return Color.red;
			case 1:
				return Color.green;
			case 2:
				return Color.blue;
			case 3:
				return Color.orange;
			case 4:
				return Color.magenta;
			default:
				return Color.red;
			}
		}
	}

}
