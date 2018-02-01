package core.loader;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import com.udojava.evalex.Expression;
import com.udojava.evalex.Expression.ExpressionException;

import core.common.DataHolder;
import core.common.DataSourceConfig;

public class ExprEvaluator extends DataLoader {
	
	private static final DataLoader expreval = new ExprEvaluator();
	
	private ExprEvaluator(){}
	
	public static DataLoader newInstance() {
		return expreval;
	}
	
	private String evalExpr(String expr)  throws ExpressionException {
		BigDecimal result = null;
		String res = null;
		Expression ex = new Expression(expr);
		//ex.setPrecision(2);
		try {
			result = ex.eval();
		} catch (ExpressionException e) {
			expr = expr.replaceAll("\\(", "");
			expr = expr.replaceAll("\\)", "");
			return expr;
		}
		res = String.valueOf(result.doubleValue());
		return res;
	}

	@Override
	public String fill(DataHolder dh) {
		String res = null;
		String expr = dh.getExpr();
		String val = (String)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr)) return null;
		if (val != null && !"".equals(val)) return (String)dh.getValue();
		
		//parse expr to see if there are any variables contained
		System.out.println(expr);
		String varexpr = expr.replaceFirst(".*\\$\\{", "");
		varexpr = varexpr.replaceFirst("\\}.*", "");
		System.out.println(varexpr);
		if (varexpr != null && !varexpr.equals(expr)) {
			//DataHolder dhInExpr = dh.getDataSource().getDataHolder(varexpr);
			DataHolder dhInExpr = DataSourceConfig.newInstance().getDataHolder(varexpr);
			if (dhInExpr == null) varexpr = "[ " + varexpr + " is not a variable, please check configuration or template!]";
			else if (dhInExpr.getValue()!=null && !"".equals(dhInExpr.getValue()))
					varexpr = expr.replaceAll(java.util.regex.Pattern.quote("${"+varexpr+"}"), (String)(dhInExpr.getValue()));
			else if (dhInExpr.getExpr()!=null && !"".equals(dhInExpr.getExpr()))
				varexpr = expr.replaceAll(java.util.regex.Pattern.quote("${"+varexpr+"}"), "("+Matcher.quoteReplacement(dhInExpr.getExpr()) + ")");
			dh.setExpr(varexpr);
			return fill(dh);
		} else {
			res = evalExpr(expr);
			dh.setValue(res);
		}
		System.out.println(res);
		return res;
	}
}
