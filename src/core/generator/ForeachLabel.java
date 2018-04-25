package core.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.aspose.words.AutoFitBehavior;
import com.aspose.words.Cell;
import com.aspose.words.CompositeNode;
import com.aspose.words.Node;
import com.aspose.words.NodeType;
import com.aspose.words.Row;
import com.aspose.words.Table;

/**
 * 统一Word报告生成系统（UWR）
 * <foreach>标签类

 * 
 */
public class ForeachLabel {
	//变量名
	private String varname = null;
	//开始节点
	private Node begin = null;
	//结束节点
	private Node end = null;
	//父集合
	private Node family = null;
	//是否在表格中
	private boolean inTable = false;
	//原始开始节点
	private Node obegin = null;
	//原始结束节点
	private Node oend = null;
	//原始父集合节点
	private Node ofamily = null;
	//是否按列展开
	private boolean isColumn = false;
	//default value when no item
	private String defaultvalue = "";
	//separator1 and separator2
	private String midsep = "";
	private String endsep = "";
	//paragraph: all the sibling nodes in between begin and end,
	//table: first element is the row, others are cells contained.
	private List<Node> nodelist = new ArrayList<Node>();

	public String getMidsep() {
		return midsep;
	}

	public void setMidsep(String midsep) {
		this.midsep = midsep;
	}

	public String getEndsep() {
		return endsep;
	}

	public void setEndsep(String endsep) {
		this.endsep = endsep;
	}

	//是否在其中
	public boolean isPart(Node node) {
		boolean flag = false;
		Node n = makeNormalNode(node);
		if (this.getNodesInRange() == null)
			this.fillNodesInBetween();
		for (Node tmp : nodelist) {
			if (tmp == n) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	//节点正则化
	public static CompositeNode<?> makeNormalNode(Node node){
		Node tmp = node;
		
		while (node != null  && node.getNodeType() != NodeType.CELL) {
			node = node.getParentNode();
		}
		if (node == null) {// 节点不是一个table cell
			node = tmp;
			while (node != null && node.getNodeType() != NodeType.PARAGRAPH ) {
				node = node.getParentNode();
			}
		}
		return (CompositeNode<?>) node;
	}
	
	//父集合节点正则化
	public static CompositeNode<?> makeNormalFamily(Node f){	
		while (f != null && f.getNodeType() != NodeType.BODY && f.getNodeType() != NodeType.TABLE) {
			f = f.getParentNode();
		}
		return (CompositeNode<?>) f;
	}
	
	//删除原始信息
	public void removeOrigin() throws Exception {
		//delete the original data
		if (isInTable()) {
			if(isColumn) {
				Iterator<Node> itr = nodelist.iterator();
				while (itr.hasNext()) {
					Node onode = itr.next();
					Node node = onode;
					while (node != null && node.getNodeType() != NodeType.ROW) {
						node = node.getParentNode();
					}
					((Row) node).removeChild(onode);
				}
				((Table)getFamily()).autoFit(AutoFitBehavior.AUTO_FIT_TO_WINDOW);
			} else {
				Set<Row> rowset = new LinkedHashSet<Row>();
				rowset.add((Row)begin.getParentNode());
				rowset.add((Row)end.getParentNode());
				Iterator<Row> itr = rowset.iterator();
				while(itr.hasNext())
					this.getFamily().removeChild(itr.next());
			}
		} else {
			for (Node t : nodelist)
				if (t != null)
					this.getFamily().removeChild(t);
		}
	}
	
	//获取范围
	public List<Node> getNodesInRange(){
		return nodelist;
	}
	
	//获取中间所有节点
	//顺序：reverse order
	public void fillNodesInBetween() {
		Node begin = this.getBegin();
		Node end = this.getEnd();
		Node node = end;
		
		nodelist.clear();
		
		if (!isInTable()) {
			nodelist.add(end);
			if (begin!=end) {				
				while (node != null && node.getPreviousSibling() != begin) {
					node = node.getPreviousSibling();
					nodelist.add(node);
				}
			}
		} else { //在table中
			Row curRow = (Row) end.getParentNode();
			Cell ocell = curRow.getFirstCell();
			while (node != begin) {
				while(node != ocell) {
					nodelist.add(node);
					node = node.getPreviousSibling();
					if(node == begin) break;
				}
				if(node == begin) break;
				nodelist.add(ocell);
				curRow=(Row) curRow.getPreviousSibling();
				ocell = curRow.getFirstCell();
				node = curRow.getLastCell();
			}
		}
		if (begin!=end) {	
			nodelist.add(begin);
		}
	}

	public String getVarname() {
		return varname;
	}

	public void setVarname(String varname) {
		this.varname = varname;
	}
	
	public String getDefaultValue() {
		return this.defaultvalue;
	}

	public void setDefaultValue(String dv) {
		if (dv == null) return;
		this.defaultvalue = dv;
	}

	public Node getBegin() {
		return begin;
	}

	public void setBegin(Node begin) {
		this.obegin = begin;
		while(this.obegin != null && this.obegin.getNodeType()!=NodeType.PARAGRAPH)
			this.obegin = this.obegin.getParentNode();
		if(this.obegin == null) this.obegin = begin;
		this.begin = makeNormalNode(begin);
		if (getBegin().getNodeType() == NodeType.CELL) 
			setInTable(true);
		else
			setInTable(false);
	}

	public Node getEnd() {
		return end;
	}

	public void setEnd(Node end) {
		this.oend = end;
		while (this.oend != null && this.oend.getNodeType() != NodeType.PARAGRAPH ) {
			this.oend = this.oend.getParentNode();
		}
		if(this.oend == null) this.oend = end;
		this.end = makeNormalNode(end);
		
		if (getEnd().getNodeType() == NodeType.CELL) 
			//when the two nodes in the same cell, will be treated as not in table.
			if (this.begin == this.end) {
				this.begin = obegin;
				this.end = oend;
				this.family = ofamily;
				while (this.family != null && this.family.getNodeType() != NodeType.CELL ) {
					this.family = this.family.getParentNode();
				}
				setInTable(false);
			} else {
				setInTable(true);
			}	
		else
			setInTable(false);
	}

	public CompositeNode<?> getFamily() {
		return (CompositeNode<?>) family;
	}

	public void setFamily(Node f) {
		this.ofamily = f;
		this.family = makeNormalFamily(f);
		if (this.family!=null && this.family.getNodeType() == NodeType.TABLE) 
			setInTable(true);
		else
			setInTable(false);		
	}

	public boolean isInTable() {
		return inTable;
	}

	private void setInTable(boolean inTable) {
		this.inTable = inTable;
	}

	//高维展开
	public void expand(List<Node> inserted) throws Exception {
		
		if (!isInTable())
			expandInParagraph(inserted);
		else // in table
			if (this.isColumn)
				expandInVerticalTable(inserted);
			else 
				expandInHorizontalTable(inserted);
	}
	
	//竖向展开
	private void expandInVerticalTable(List<Node> inserted) throws Exception {
		Map<Node, List<Node>> map = new LinkedHashMap<>();
		Iterator<Node> itr = nodelist.iterator();
		while (itr.hasNext()) {
			Node onode = itr.next();
			Node node = onode;
			while (node != null  && node.getNodeType() != NodeType.ROW) {
				node = node.getParentNode();
			}
			List<Node> lns = map.get(node);
			if(lns == null) lns = new ArrayList<>();
			lns.add(0,onode);
			map.put(node, lns);
		}
		
		Set<Node> keys = map.keySet();
		itr = keys.iterator();
		while(itr.hasNext()) {
			Node node = itr.next();
			List<Node> lns = map.get(node);
			for(int i = lns.size()-1; i>=0; i--) {
				Node newnode = ((Cell)lns.get(i)).deepClone(true);
				((Row)node).insertAfter(newnode, lns.get(0));
				inserted.add(newnode);
			}
		}
	}

	//段落展开
	private void expandInParagraph(List<Node> inserted) throws Exception {
		for (Node tmp : nodelist) {
			if (tmp != null) {
				Node newnode = tmp.deepClone(true);
				getFamily().insertAfter(newnode, getEnd());
				inserted.add(newnode);
			}
		}
	}
	
	//横向展开
	private void expandInHorizontalTable(List<Node> inserted) throws Exception {
		Set<Row> rowset = new LinkedHashSet<Row>();
		for (int z = nodelist.size() - 1; z >= 0; z--) {
			Node tmp = nodelist.get(z);
			rowset.add((Row) tmp.getParentNode());
		}

		Iterator<Row> itr = rowset.iterator();
		Row index = null;
		while (itr.hasNext()) {
			Row curRow = itr.next();
			Row row = (Row) curRow.deepClone(false);
			for (Cell ocell : curRow.getCells()) {
				boolean isCopied = false;
				Cell cell = null;
				for (int z = nodelist.size() - 1; z >= 0; z--) {
					Node tmp = nodelist.get(z);
					if (tmp == ocell) {
						isCopied = true;
						cell = (Cell) tmp.deepClone(true);
						row.appendChild(cell);
						inserted.add(cell);
					}
				}
				if (!isCopied) {
					cell = (Cell) ocell.deepClone(true);
					row.appendChild(cell);
				}
			}
			if (index == null) {
				getFamily().insertAfter(row, (Node) (rowset.toArray()[rowset.size() - 1]));
				index = row;
			} else
				getFamily().insertAfter(row, index);
		}
	}

	public void setColumn(boolean b) {
		isColumn = b;	
	}

	//使用默认值展开
	public void expandWithDefaultValue() throws Exception {
		if (isInTable()) { // default value not supported in table!
			return;
		} else {
			for (Node t : nodelist)
				if (t != null) {
					t.getRange().replace(Pattern.compile("<.*>"), this.getDefaultValue());
				}
		}
	}
}
