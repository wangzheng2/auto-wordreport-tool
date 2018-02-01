package core.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

public abstract class DataHolder implements DataType {
	private String type = VALUE;
	private String name = null;
	private DataSource ds = null;
	private String expr = null;
	private int processloc = 0;
	private HolderFiller dataFiller = null;
	private HolderRender dataRender = null;

	private Object value = null;

	DataHolder(DataSource ds, String name, String type){
		this.ds = ds;
		setName(name);
		setType(type);
	}
	
	public  String fillValue() throws Exception {
		
		if (this.getValue() != null)  return null; //already filled
		if (this.getHolderFiller() != null)
			return getHolderFiller().fill(this);
		else
			return null;
	}
	
	public  int renderValue(ReplacingArgs e, String[] varinfo) throws Exception {
		if (getHolderRender()!=null)
			return getHolderRender().render(this, e, varinfo);
		return ReplaceAction.SKIP;
	}
	
	public HolderRender getHolderRender() {
		return dataRender;
	}

	public void setHolderRender(HolderRender dataRender) {
		this.dataRender = dataRender;
	}
	
	public HolderFiller getHolderFiller() {
		return dataFiller;
	}

	public void setHolderFiller(HolderFiller dataFiller) {
		this.dataFiller = dataFiller;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if(VALUE.equalsIgnoreCase(type.trim()))
			this.type = VALUE;
		else if(LIST.equalsIgnoreCase(type.trim()))
			this.type = LIST;
		else if(MAP.equalsIgnoreCase(type.trim()))
			this.type = MAP;
		else
			this.type = NA;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		//名字不分大小写，程序内部全部转成小写
		if (name != null)
			this.name = name.trim().toLowerCase();
		else
			this.name = "";
	}

	public DataSource getDataSource() {
		return ds;
	}

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	public int getProcessloc() {
		return processloc;
	}

	public void setProcessloc(int processloc) {
		this.processloc = processloc;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public abstract int size();
	
	public String sysdate(String format){
		String fmt = null;
		Date date = new Date();
		if (format == null || "".equals(format)) 
			fmt = "YYYY年MM月dd日";
		else
			fmt = format;
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		return sdf.format(date);
	}
	
	public String trim(String attrname) {
		Object val = this.getValue();
		String s = null;
		if (val instanceof String) s = (String) val;
		else return null;
		if (s != null) s=s.trim();
		this.setValue(s);//this will bring side-effect!
		return s;
	}
	
	public String replace(String parms) {
		Object val = this.getValue();
		String s = null;
		if (val instanceof String) s = (String) val;
		else return null;
		
		String parm[] = parms.split(",");
		if (parm.length != 2) return null;
		if (parm[0]==null) return null;
		if (parm[1]==null) return null;
		if ("null".equalsIgnoreCase(parm[1])) parm[1] = "";
		s=s.replaceFirst(parm[0], parm[1]);
		return s;
	}
	
	public String replace2(String parms) {
		Object val = this.getValue();
		String s = null;
		if (val instanceof String) s = (String) val;
		else return null;
		
		String parm[] = parms.split(",");
		if (parm.length != 4) return null;
		if (parm[0]==null) return null;
		if (parm[1]==null) return null;
		if ("null".equalsIgnoreCase(parm[1])) parm[1] = "";
		if (parm[2]==null) return null;
		if (parm[3]==null) return null;
		if ("null".equalsIgnoreCase(parm[3])) parm[3] = "";
		if (s.matches(parm[0]))
			s=s.replaceFirst(parm[0], parm[1]);
		else
			s=s.replaceFirst(parm[2], parm[3]);
		return s;
	}
	
	public  long count(String attrname) {
		return size();
	}
	
	public  abstract double sum(String attrname);
	
}
