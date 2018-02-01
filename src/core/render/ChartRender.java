package core.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.List;

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
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.Node;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

import core.common.DataHolder;
import core.common.HolderRender;

public class ChartRender implements HolderRender {

	@Override
	public int render(DataHolder dh, ReplacingArgs e, String[] varinfo) throws Exception {
		Node node = e.getMatchNode();
		DocumentBuilder builder = new DocumentBuilder((Document) node.getDocument());
		int height = 0, width = 0;
		
		if (dh == null) return ReplaceAction.SKIP;
		if(dh.getValue() == null) dh.fillValue();
		
		String chart = null;
		
		for (int i = 0; i < varinfo.length; i++) {
			if (varinfo[i].matches("chart=\".*?\"")) {
				chart = varinfo[i].toLowerCase().replaceFirst("chart=\"", "");
				chart = chart.replaceFirst("\"", "");
				chart = chart.replaceFirst("\\$\\{", "");
				chart = chart.replaceFirst("\\}", "");
				System.out.println("chart: " + chart);
				break;
			}
		}
		
		if (chart == null || "".equals(chart)) chart="pie";
		@SuppressWarnings("unchecked")
		List<AbstractDataset> datasets = (List<AbstractDataset>) dh.getValue();
		if(datasets == null || datasets.size() != 3) return ReplaceAction.SKIP;
		JFreeChart fchart = null;
		if ("bar1".equals(chart)) {fchart = makeBarChart(datasets.get(0),"违反次数最多的规则(前五)", "规则名", "违反数"); width = 600; height = 400;}
		else if ("bar2".equals(chart)) {fchart = makeBarChart(datasets.get(1),"文件规则违反统计图(前五)", "文件名", "违反数"); width = 600; height = 400;}
		else if ("pie".equals(chart)) {fchart = makePieChart(datasets.get(2),"违反规则优先级"); width = 600; height = 300;}
		if (fchart == null) return ReplaceAction.SKIP;	
		updateJFreeChartBeforeExport(fchart);
		
		BufferedImage bufferedImage = fchart.createBufferedImage(width, height);
		builder.moveTo(node);
		builder.insertImage(bufferedImage);
		return ReplaceAction.REPLACE;
	}
	
	public JFreeChart makePieChart(AbstractDataset ds, String title) {
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
	
	public JFreeChart makeBarChart(AbstractDataset ds, String title, String axisX, String axisY) {
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
