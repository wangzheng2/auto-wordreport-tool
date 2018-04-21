package core.loader;

import java.math.BigDecimal;
import java.util.regex.Matcher;

import org.apache.logging.log4j.Logger;

import com.udojava.evalex.Expression;
import com.udojava.evalex.Expression.ExpressionException;

import core.common.DataHolder;
import core.common.DataSourceConfig;
import core.generator.ReportGenerator;

/**
 * 统一Word报告生成系统（UWR）
 * 常量表达式处理类
 * @author 王铮
 * @author 朴勇 15641190702
 * 
 */
public class ExprEvaluator extends DataLoader {
	
	private static final DataLoader expreval = new ExprEvaluator();
	private Logger logger = ReportGenerator.getLogger();
	
	private ExprEvaluator(){}
	
	public static DataLoader newInstance() {
		return expreval;
	}
	
	//表达式计算
	private String evalExpr(String expr)  throws ExpressionException {
		BigDecimal result = null;
		String res = null;
		Expression ex = new Expression(expr);
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

	//填充
	@Override
	public String fill(DataHolder dh) {
		String res = null;
		String expr = dh.getExpr();
		String val = (String)dh.getValue();
		
		if (dh == null || expr == null || "".equals(expr)) return null;
		if (val != null && !"".equals(val)) return (String)dh.getValue();
		
		//是否有变量？
		logger.debug(expr);
		String varexpr = expr.replaceFirst(".*\\$\\{", "");
		varexpr = varexpr.replaceFirst("\\}.*", "");
		logger.debug(varexpr);
		if (varexpr != null && !varexpr.equals(expr)) {
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
		logger.debug(res);
		return res;
	}
}
