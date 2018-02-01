package core.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import core.common.CollectionHolder;
import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.common.DbDataSource;
import core.common.ListHolder;
import core.common.MapHolder;
import core.common.VarHolder;
import core.render.LiteralRender;

public class DbLoader extends DataLoader {
	
	private static DataLoader dbLoader = new DbLoader();
	
	private DbLoader() {};
	
	public static DataLoader newInstance() {
		return dbLoader;
	}
	
	private Connection getConnection(DataHolder dh) throws ClassNotFoundException, SQLException {
		DbDataSource ds = (DbDataSource)(dh.getDataSource());
		return ds.getConnection();
	}
	
	private String queryResult(DataHolder dh) throws Exception {
		List<DataHolder> nodedhs = new ArrayList<DataHolder>();
		String sql = dh.getExpr();
        Connection conn = this.getConnection(dh);
        Statement stmt  = conn.createStatement();
        ResultSet rs    = stmt.executeQuery(sql);

		ResultSetMetaData meta = rs.getMetaData();
		CollectionHolder ch = new ListHolder (dh.getDataSource(), "nodes", nodedhs, LiteralRender.newInstance());
		dh.setValue(ch);
		// loop through the result set
		int j= 0;
		while (rs.next()) {	
			List<DataHolder> attrdhs = new ArrayList<DataHolder>();
			DataHolder mapdh = new MapHolder(dh.getDataSource(), meta.getTableName(1)+"_"+j++, attrdhs, LiteralRender.newInstance());
			mapdh.setHolderRender(LiteralRender.newInstance());
			nodedhs.add(mapdh);
			//add a sequence attribute by default
			attrdhs.add(new VarHolder(dh.getDataSource(), "rawid", String.valueOf(j), LiteralRender.newInstance()));
			for (int i = 0; i < meta.getColumnCount(); i++) {
				String columnLabel = meta.getColumnLabel(i + 1);
				DataHolder vardh = new VarHolder(dh.getDataSource(), columnLabel, rs.getObject(columnLabel).toString());
				vardh.setHolderRender(LiteralRender.newInstance());
				System.out.println(meta.getColumnName(i + 1)+": "+vardh.getValue());
				attrdhs.add(vardh);
			}
		}
		rs.close();
		stmt.close();
		return String.valueOf(nodedhs.size());
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
