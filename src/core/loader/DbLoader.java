package core.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.logging.log4j.Logger;

import core.common.CollectionHolder;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.common.DbDataSource;
import core.common.ListHolder;
import core.common.MapHolder;
import core.common.VarHolder;
import core.generator.ReportGenerator;
import core.render.LiteralRender;

/**
 * 统一Word报告生成系统（UWR）
 * 关系型数据库数据加载器类（单例）

 * 
 */
public class DbLoader extends DataLoader {
	
	private static DataLoader dbLoader = new DbLoader();
	private Logger logger = ReportGenerator.getLogger();
	
	private DbLoader() {};
	
	public static DataLoader newInstance() {
		return dbLoader;
	}
	//获取连接
	private Connection getConnection(DataHolder dh) throws ClassNotFoundException, SQLException {
		DbDataSource ds = (DbDataSource)(dh.getDataSource());
		return ds.getConnection();
	}
	//获取数据并挂载到数据源
	private String queryResult(DataHolder dh) throws Exception {
		List<DataHolder> nodedhs = new ArrayList<DataHolder>();
		String sql = dh.getExpr();
        Connection conn = this.getConnection(dh);
        Statement stmt  = conn.createStatement();
        ResultSet rs    = stmt.executeQuery(sql);

		ResultSetMetaData meta = rs.getMetaData();
		CollectionHolder ch = new ListHolder (dh.getDataSource(), "nodes", nodedhs, LiteralRender.newInstance());
		dh.setValue(ch);
		//结果集循环处理
		int j= 0;
		while (rs.next()) {	
			List<DataHolder> attrdhs = new ArrayList<DataHolder>();
			DataHolder mapdh = new MapHolder(dh.getDataSource(), meta.getTableName(1)+"_"+j++, attrdhs, LiteralRender.newInstance());
			mapdh.setHolderRender(LiteralRender.newInstance());
			nodedhs.add(mapdh);
			//默认添加rawid属性
			attrdhs.add(new VarHolder(dh.getDataSource(), "rawid", String.valueOf(j), LiteralRender.newInstance()));
			for (int i = 0; i < meta.getColumnCount(); i++) {
				String columnLabel = meta.getColumnLabel(i + 1);
				Object columnObject = rs.getObject(columnLabel);
				if (columnLabel!=null && !"".equals(columnLabel) && columnObject != null) {
					DataHolder vardh = new VarHolder(dh.getDataSource(), columnLabel, rs.getObject(columnLabel).toString());
					vardh.setHolderRender(LiteralRender.newInstance());
					logger.debug(meta.getColumnName(i + 1) + ": " + vardh.getValue());
					attrdhs.add(vardh);
				}
			}
		}
		rs.close();
		stmt.close();
		return String.valueOf(nodedhs.size());
	}
	
	//填充
	@Override
	public String fill(DataHolder dh) throws Exception {
		String res = null;
		String expr = dh.getExpr();
		String oexpr = expr;
	
		CollectionHolder val = (CollectionHolder)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr) || val!=null) return String.valueOf(0);
			
		//是否有变量的引用？
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
