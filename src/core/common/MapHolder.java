package core.common;

import java.util.ArrayList;
import java.util.List;

public class MapHolder extends CollectionHolder {

	public MapHolder(DataSource ds, String name, ArrayList<DataHolder> vars) {
		super(ds, name, MAP, vars);
	}
	
	public MapHolder(DataSource ds, String name, List<DataHolder> vars, HolderRender render) {
		super(ds, name, MAP, vars);
		this.setHolderRender(render);
	}

}
