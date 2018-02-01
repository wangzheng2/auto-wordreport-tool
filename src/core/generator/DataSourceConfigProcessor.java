package core.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import core.common.*;
import core.loader.ExprEvaluator;
import core.render.LiteralRender;

public class DataSourceConfigProcessor extends DefaultHandler implements ErrorHandler, DataSourceType, DataType {
	
	private int i = 0;
	private DataSourceConfig dsc = null;
	//处理中的DataSource
	private DataSource ds = null;
	//考虑变量定义可能嵌套的情况
	private List<DataHolder> dhs = new ArrayList<DataHolder>();
	DataHolder dh = null;
	private List<String> labels = new ArrayList<String>();
	
	public DataSourceConfigProcessor(String filename){
		dsc = DataSourceConfig.newInstance();
		dsc.setFilename(filename);
	}

	private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
	
	public void parseConfigFile() {

		String xsdFileName = "resource/validate.xsd";
		
		//重复解析
		if (dsc == null || dsc.getDataSources() != null) return;
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
	    spf.setNamespaceAware(true);
	    SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	    try {
	    	spf.setSchema(schemaFactory.newSchema(new StreamSource(new BufferedReader(new FileReader(xsdFileName)))));
			SAXParser saxParser = spf.newSAXParser();			
			XMLReader xmlReader = saxParser.getXMLReader();			
			xmlReader.setContentHandler(this);
			xmlReader.setErrorHandler(this);
			xmlReader.parse(convertToFileURL(dsc.getFilename()));				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startDocument() throws SAXException {
		ArrayList<DataSource> dss = new ArrayList<DataSource>();
		dsc.setDataSources(dss);
        System.out.println("begin doc!");
    }
	
	@Override
	public void endDocument() throws SAXException {
		System.out.println("end doc!");
	}
	
	@Override
	public void startElement(String namespaceURI, String localName, String qName,  Attributes atts) throws SAXException {

		i++;
		String type=null;
		String name = null;
		String expr = null;
		String localname = localName.toLowerCase();
		
		if (atts != null) {
			type = atts.getValue("type");
			name = atts.getValue("name");
			expr = atts.getValue("expr");
			if (expr == null || "".equals(expr)) expr = atts.getValue("query");
		}
		
		if (("password".equals(localname) || "username".equals(localname) || "driver".equals(localname) || "url".equals(localname) || "var".equals(localname) || "file".equals(localname) || "path".equals(localname)) 
				&& ds != null) {
			labels.add(localname);
		}
		
		if ("var".equalsIgnoreCase(localName) && ds != null ) {
			System.out.println(localName + "["+i+"]" +  " type:" + type);
			if (VALUE.equalsIgnoreCase(type)) {
				dh = new VarHolder(ds, name, null, LiteralRender.newInstance());
				dh.setExpr(expr);
				dh.setHolderFiller(ExprEvaluator.newInstance());
				//取最后一个DataHolder放入
				if (dhs.size() > 0) {
					List<DataHolder> hs = ((CollectionHolder)(dhs.get(dhs.size() - 1))).getVars();
					hs.add(dh);
				}
				//这里不清理dh！
			} else if (LIST.equalsIgnoreCase(type)) {
				dh = new ListHolder(ds, name, null, LiteralRender.newInstance());
				dh.setExpr(expr);
				dhs.add(dh);
				dh = null;
			} else { // type = "MAP"
				dh = new MapHolder(ds,name,null, LiteralRender.newInstance());
				dh.setExpr(expr);
				dhs.add(dh);
				dh = null;
			}
		}
		
		if ("datasource".equalsIgnoreCase(localName)) {
			
			if (CONST.equalsIgnoreCase(type)) {
				ds = new ConstDataSource();
			} else if (DB.equalsIgnoreCase(type)) {
				ds = new DbDataSource(name, "", "", "", "");
			} else if (XML.equalsIgnoreCase(type)) {
				ds = new XmlDataSource(name, "", true);
			} else if (JSON.equalsIgnoreCase(type)) {
				// to be added!
			} else {
				ds = new ImgDataSource(name,"",true);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		String localname = localName.toLowerCase();
		if ("password".equals(localname) || "username".equals(localname) || "driver".equals(localname) || "url".equals(localname) || "var".equals(localname) || "file".equals(localname) || "path".equals(localname)) 
			labels.remove(labels.size() - 1);
	
		if ("var".equalsIgnoreCase(localName)) {

			if (dh == null ) { //dh == null则本次处理的是一个Collection
				dh = dhs.remove(dhs.size() - 1);
				//挂载到前面的DataHolder上
				if (dhs.size() > 0) 
					((CollectionHolder)(dhs.get(dhs.size() - 1))).getVars().add(dh);
			}
			//前面没有DataHolder了，则挂载到ConstDataSource下
			if (dhs.size() <= 0) 
				((ConstDataSource)(dh.getDataSource())).getVars().add(dh);
			
			dh = null;
		}
		//清除ds
		if ("datasource".equalsIgnoreCase(localName)) {
			dsc.getDataSources().add(ds);
			ds = null;
		}
	}
	
	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
		
		if (labels.size() > 0) {
			if ("var".equals(labels.get(labels.size() - 1))) {
				String value = new String(ch, start, length);
				if (dh != null) ((VarHolder) dh).setValue(value);
			}
			if ("file".equals(labels.get(labels.size() - 1)) || "path".equals(labels.get(labels.size() - 1))) {
				String path = new String(ch, start, length);
				if (ds != null) ((StreamDataSource) ds).setPath(path);
			}
			if ("url".equals(labels.get(labels.size() - 1))) {
				String url = new String(ch, start, length);
				if (ds != null) ((DbDataSource) ds).setUrl(url);
			}
			if ("driver".equals(labels.get(labels.size() - 1))) {
				String driver = new String(ch, start, length);
				if (ds != null) ((DbDataSource) ds).setDriver(driver);
			}
			if ("username".equals(labels.get(labels.size() - 1))) {
				String username = new String(ch, start, length);
				if (ds != null) ((DbDataSource) ds).setUsername(username);
			}
			if ("password".equals(labels.get(labels.size() - 1))) {
				String pwd = new String(ch, start, length);
				if (ds != null) ((DbDataSource) ds).setPassword(pwd);
			}
		}
    }
	
	@Override
	public void warning(SAXParseException e) throws SAXException {
		System.out.print("Warning at line " + e.getLineNumber() + ": ");
		System.out.println(e.getMessage());
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		System.out.print("Error at line " + e.getLineNumber() + ": ");
		System.out.println(e.getMessage());
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		System.out.print("Fatal error at line " + e.getLineNumber() + ": ");
		System.out.println(e.getMessage());
	}

}
