package core.generator.Action;

import java.util.UUID;

import com.aspose.words.IReplacingCallback;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;

public class ForeachRewriteAction implements IReplacingCallback {
	@Override
	public int replacing(ReplacingArgs e) throws Exception {
		String def_foreach_orig = e.getMatch().group();	
		String def_name = UUID.randomUUID().toString();
		String def_var = def_foreach_orig.replaceFirst("foreach", "var name=\""+def_name+"\"");
		String def_foreach = def_foreach_orig.replaceFirst("foreach.*", "foreach var=\""+def_name+"\">");
		
		e.setReplacement(def_var + def_foreach);
		return ReplaceAction.REPLACE;
	}
}
