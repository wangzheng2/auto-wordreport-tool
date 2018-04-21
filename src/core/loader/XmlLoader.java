package core.loader;

import core.common.CollectionHolder;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.common.ListHolder;
import core.common.MapHolder;
import core.common.StreamDataSource;
import core.common.VarHolder;
import core.generator.ReportGenerator;
import core.render.LiteralRender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 统一Word报告生成系统（UWR）
 * XML数据加载器类（单例）
 * @author 王铮
 * @author 朴勇 15641190702
 * 
 */
public class XmlLoader extends DataLoader {
	
	private static final DataLoader xmlLoader = new XmlLoader();
	private Logger logger = ReportGenerator.getLogger();
	private static Map<String, Document> docs = new HashMap<>();
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private static XPathFactory xpathFactory = XPathFactory.newInstance();
	private static XPath xpath = xpathFactory.newXPath();
	private static DocumentBuilder builder;

	private XmlLoader() {};
	
	public static DataLoader newInstance() {
		return xmlLoader;
	}

	//获取数据
	private String queryResult(DataHolder dh) {
		factory.setNamespaceAware(true);
		Document doc = null;
		Object nodes = null;
		int elems = 0;

		try {
			builder = factory.newDocumentBuilder();
			doc = docs.get(((StreamDataSource)dh.getDataSource()).getPath());
			if (doc == null) {
				doc = builder.parse("file:///"+((StreamDataSource)dh.getDataSource()).getPath());
				docs.put(((StreamDataSource)dh.getDataSource()).getPath(),doc);
			}
			XPathExpression expr = xpath.compile(dh.getExpr());
			if (dh.getExpr().matches("^/.*"))
				nodes = expr.evaluate(doc, XPathConstants.NODESET);
			else
				nodes = expr.evaluate(doc, XPathConstants.NUMBER);
			elems = transHolder(dh, nodes);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error(e);
		} catch (XPathExpressionException e) {
			logger.error(e);
		}
		return String.valueOf(elems);
	}
	
	//转换格式
	protected int transHolder(DataHolder dh, Object nlist) {
		List<DataHolder> nodedhs = new ArrayList<DataHolder>();
		NodeList nodelist = null;
		
		if (dh == null || nlist == null || dh.getValue() != null) return 0;
		CollectionHolder ch = new ListHolder (dh.getDataSource(), "nodes", nodedhs, LiteralRender.newInstance());
		dh.setValue(ch);
		if (nlist instanceof NodeList) nodelist = (NodeList) nlist;
		else {
			Number num = (Number) nlist;
			List<DataHolder> attrdhs = new ArrayList<DataHolder>();
			DataHolder mapdh = new MapHolder(dh.getDataSource(), "result", attrdhs, LiteralRender.newInstance());	
			nodedhs.add(mapdh);
			attrdhs.add(new VarHolder(dh.getDataSource(), "rawid", String.valueOf(1), LiteralRender.newInstance()));
			DataHolder vardh = new VarHolder(dh.getDataSource(), "value", String.valueOf(num), LiteralRender.newInstance());
			attrdhs.add(vardh);
			return 1;
		}
		for (int i=0; i < nodelist.getLength(); i++) {
			List<DataHolder> attrdhs = new ArrayList<DataHolder>();
			Node node = nodelist.item(i);
			if(node.getNodeName().matches("#.*")) continue;
			DataHolder mapdh = new MapHolder(dh.getDataSource(), node.getNodeName()+"_"+i, attrdhs, LiteralRender.newInstance());
			nodedhs.add(mapdh);
			//默认添加rawid属性
			attrdhs.add(new VarHolder(dh.getDataSource(), "rawid", String.valueOf(i+1), LiteralRender.newInstance()));
			if (node.getNodeType() == Node.ELEMENT_NODE ) {		
				DataHolder vardh = new VarHolder(dh.getDataSource(), "text", node.getTextContent());
				attrdhs.add(vardh);
				NamedNodeMap attrs = node.getAttributes();
				if (attrs != null)
				for (int j = 0; j < attrs.getLength(); j++) {
					Node attr = attrs.item(j);
					if (attr.getNodeType() != Node.ATTRIBUTE_NODE) continue;
					DataHolder attrvardh = new VarHolder(dh.getDataSource(), attr.getNodeName(),attr.getNodeValue(), LiteralRender.newInstance());
					attrdhs.add(attrvardh);
				}
				
				NodeList nl = node.getChildNodes();
				boolean found = false;
				for(int k=0; k<nl.getLength(); k++) {
					if(nl.item(k).getNodeType() == Node.ELEMENT_NODE) {
						found = true;
						break;
					}
				}
				if (found) transHolder((DataHolder)mapdh, node.getChildNodes());
				
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				DataHolder attrvardh = new VarHolder(dh.getDataSource(), node.getNodeName(), node.getNodeValue(), LiteralRender.newInstance());
				attrdhs.add(attrvardh);
			}
		}
		return nodedhs.size();
	}

	//填充
	@Override
	public String fill(DataHolder dh) throws Exception {
		String res = null;
		String expr = dh.getExpr();
		String oexpr = expr;
	
		CollectionHolder val = (CollectionHolder)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr) || val!=null) return String.valueOf(0);
			
		//是否存在变量引用？
		logger.debug(expr);
		String tmpexpr = null;
		tmpexpr = expr;
		while(expr.matches(".*?\\$\\{.*")) {
			tmpexpr = expr.replaceFirst(".*?\\$\\{", "");
			tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
			DataHolder dhh = DataSourceConfig.newInstance().getDataHolder(tmpexpr);
			if ( dhh != null) {
				if (dhh.getValue() == null) dhh.fillValue();
				oexpr = oexpr.replaceAll(java.util.regex.Pattern.quote("${"+tmpexpr+"}"), Matcher.quoteReplacement(dhh.getValue().toString()));
			}
			expr=expr.replaceFirst("\\$\\{", "");
		}
		dh.setExpr(oexpr);
		res = queryResult(dh);
		logger.debug(res);
		return res;
	}

}
