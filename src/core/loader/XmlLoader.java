package core.loader;

import core.common.CollectionHolder;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.common.ListHolder;
import core.common.MapHolder;
import core.common.StreamDataSource;
import core.common.VarHolder;
import core.render.LiteralRender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlLoader extends DataLoader {
	
	private static final DataLoader xmlLoader = new XmlLoader();
	
	private XmlLoader() {};
	
	public static DataLoader newInstance() {
		return xmlLoader;
	}

	private String queryResult(DataHolder dh) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		NodeList nodes = null;
		int elems = 0;

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(((StreamDataSource)dh.getDataSource()).getPath());
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(dh.getExpr());
			nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);		
			elems = transHolder(dh, nodes);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return String.valueOf(elems);
	}
	
	protected int transHolder(DataHolder dh, NodeList nodelist) {
		List<DataHolder> nodedhs = new ArrayList<DataHolder>();
		
		if (dh == null || nodelist == null || dh.getValue() != null) return 0;	
		CollectionHolder ch = new ListHolder (dh.getDataSource(), "nodes", nodedhs, LiteralRender.newInstance());
		dh.setValue(ch);
		for (int i=0; i < nodelist.getLength(); i++) {
			List<DataHolder> attrdhs = new ArrayList<DataHolder>();
			Node node = nodelist.item(i);
			if(node.getNodeName().matches("#.*")) continue;
			DataHolder mapdh = new MapHolder(dh.getDataSource(), node.getNodeName()+"_"+i, attrdhs, LiteralRender.newInstance());
			nodedhs.add(mapdh);
			//add a sequence attribute by default
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

	@Override
	public String fill(DataHolder dh) throws Exception {
		String res = null;
		String expr = dh.getExpr();
		String oexpr = expr;
	
		CollectionHolder val = (CollectionHolder)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr) || val!=null) return String.valueOf(0);
			
		//parse expr to see if there are any variables contained
		System.out.println(expr);
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
		System.out.println(res);
		return res;
	}

}
