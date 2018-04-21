package core.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

/**
 * 统一Word报告生成系统（UWR）
 * 抽象数据类
 * @author 朴勇 15641190702
 * 
 */
public abstract class DataHolder implements DataType {
	//类型
	private String type = VALUE;
	//名字
	private String name = null;
	//所在数据源
	private DataSource ds = null;
	//关联表达式
	private String expr = null;
	//处理位置
	private int processloc = 0;
	//关联填充器
	private HolderFiller dataFiller = null;
	//关联呈现器
	private HolderRender dataRender = null;
	//值
	private Object value = null;
	//交换区
	private Object _swap = null;

	DataHolder(DataSource ds, String name, String type){
		this.ds = ds;
		setName(name);
		setType(type);
	}
	
	//数据填充
	public  String fillValue() throws Exception {
		
		if (this.getValue() != null)  return null; //already filled
		if (this.getHolderFiller() != null)
			return getHolderFiller().fill(this);
		else
			return null;
	}
	
	//数据呈现
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
	
	public Object getSwap() {
		return _swap;
	}

	public void setSwap(Object value) {
		this._swap = value;
	}
	
	public abstract int size();
	
	//返回当前日期，供回调，支持自定义格式
	public String sysdate(String format){
		String fmt = null;
		Date date = new Date();
		if (format == null || "".equals(format))
			//默认格式
			fmt = "YYYY年MM月dd日";
		else
			fmt = format;
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		this.setSwap(sdf.format(date));
		return sdf.format(date);
	}
	
	//去掉前后空格，供回调
	public String trim(String attrname) {
		Object val = this.getValue();
		String s = null;
		if (val instanceof String) s = (String) val;
		else return null;
		if (s != null) s=s.trim();
		this.setSwap(s);
		return s;
	}
	
	//值替换，供回调
	public String replace(String parms) {
		Object val = null;
		if (_swap != null)
			val = this.getSwap();
		else
			val = this.getValue();
		String s = null;
		if (val instanceof String) s = (String) val;
		else return null;
		
		String parm[] = parms.split(",");
		if (parm.length != 2) return null;
		if (parm[0]==null) return null;
		if (parm[1]==null) return null;
		if ("null".equalsIgnoreCase(parm[1])) parm[1] = "";
		s=s.replaceFirst(parm[0], parm[1]);
		this.setSwap(s);
		return s;
	}
	
	//四舍五入，供回调
	public String format(String parms) {
		Object val = null;
		if (_swap != null)
			val = this.getSwap();
		else
			val = this.getValue();
		String s = null;
		if (val instanceof String) s = (String) val;
		else return null;
		
		if(parms == null || "".equals(parms)) return null;
		double d = Double.valueOf(s);
		s = String.format(parms, d);
		this.setSwap(s);
		return s;
	}
	
	//值替换，供回调
	public String replace2(String parms) {
		Object val = null;
		if (_swap != null)
			val = this.getSwap();
		else
			val = this.getValue();
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
		this.setSwap(s);
		return s;
	}
	
	//返回个数，供回调
	public  long count(String attrname) {
		this.setSwap(String.valueOf(size()));
		return size();
	}
	
	//返回和值，供回调
	public  abstract double sum(String attrname);

	//返回最大值，供回调
	public  abstract double max(String attrname);

	//返回最小值，供回调
	public  abstract double min(String attrname);

	//返回平均值，供回调
	public  double avg(String attrname) {
		long cnt = count(attrname);
		double sums = sum(attrname);
		double avgs = 0;
		if (cnt != 0) {
            avgs = sums/cnt;
			this.setSwap(String.valueOf(avgs));
		}
		return avgs;
	}
}
