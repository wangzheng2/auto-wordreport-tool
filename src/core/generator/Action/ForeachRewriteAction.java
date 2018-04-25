package core.generator.Action;

import com.aspose.words.*;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * 统一Word报告生成系统（UWR）
 * <foreach>标签重写类

 * 
 */
public class ForeachRewriteAction implements IReplacingCallback {
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String def_foreach_orig = e.getMatch().group();
		Node node0 = e.getMatchNode();

		if(!def_foreach_orig.matches(".*?ds=.*"))
			return ReplaceAction.SKIP;

		String def_name = getSaltString();
		String def_var = def_foreach_orig.replaceFirst("foreach", "var name=\""+def_name+"\"");
		def_var = def_var.replaceFirst(">", "/>");
		String def_foreach = def_foreach_orig.replaceFirst("foreach.*", "foreach var=\""+def_name+"\">");

		while (node0 != null && node0.getNodeType()!=NodeType.PARAGRAPH)
			node0 = node0.getParentNode();
		if (node0 != null && node0.getNodeType()==NodeType.PARAGRAPH) {
			Paragraph node1 = (Paragraph) node0.deepClone(true);
			node0.getRange().replace(Pattern.compile(".*"), def_var);
			node1.getRange().replace(Pattern.compile(".*"), def_foreach);
			node0.getParentNode().insertAfter(node1,node0);
		}
		return ReplaceAction.SKIP;
	}

	protected String getSaltString() {
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 8) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;
	}
}
