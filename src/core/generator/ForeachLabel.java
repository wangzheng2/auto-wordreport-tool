package core.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.aspose.words.Cell;
import com.aspose.words.CompositeNode;
import com.aspose.words.Node;
import com.aspose.words.NodeType;
import com.aspose.words.Row;

public class ForeachLabel {
	private String varname = null;
	private Node begin = null;
	private Node end = null;
	private Node family = null;
	private boolean inTable = false;
	//paragraph: all the sibling nodes in between begin and end,
	//table: first element is the row, others are cells contained.
	private List<Node> nodelist = new ArrayList<Node>();
	
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
	
	public static CompositeNode<?> makeNormalNode(Node node){
		Node tmp = node;
		
		while (node != null  && node.getNodeType() != NodeType.CELL) {
			node = node.getParentNode();
		}
		if (node == null) {// node is not a table cell	
			node = tmp;
			while (node != null && node.getNodeType() != NodeType.PARAGRAPH ) {
				node = node.getParentNode();
			}
		}		
		return (CompositeNode<?>) node;
	}
	
	public static CompositeNode<?> makeNormalFamily(Node f){	
		while (f != null && f.getNodeType() != NodeType.BODY && f.getNodeType() != NodeType.TABLE) {
			f = f.getParentNode();
		}
		return (CompositeNode<?>) f;
	}
	
	public void removeOrigin() {
		//delete the original data
		if (isInTable()) {
			Set<Row> rowset = new LinkedHashSet<Row>();
			rowset.add((Row)begin.getParentNode());
			rowset.add((Row)end.getParentNode());
			Iterator<Row> itr = rowset.iterator();
			while(itr.hasNext())
				this.getFamily().removeChild(itr.next());
		} else {
			for(Node t:nodelist)
				this.getFamily().removeChild(t);
		}
	}
	
	public List<Node> getNodesInRange(){
		return nodelist;
	}
	
	//in reverse order
	public void fillNodesInBetween() {
		Node begin = this.getBegin();
		Node end = this.getEnd();
		Node node = end;
		
		if (!isInTable()) {
			nodelist.add(end);
			if (begin!=end) {				
				while (node != null && node.getPreviousSibling() != begin) {
					node = node.getPreviousSibling();
					nodelist.add(node);
				}
				nodelist.add(begin);
			}
		} else { // in table
			Row curRow = (Row) end.getParentNode();
			Cell ocell = curRow.getFirstCell();
			while (node != begin) {
				while(node != ocell) {
					nodelist.add(node);
					node = node.getPreviousSibling();
					if(node == begin) break;
				}
				if(node == begin) {
					nodelist.add(begin);
					break;
				}
				nodelist.add(ocell);
				curRow=(Row) curRow.getPreviousSibling();
				ocell = curRow.getFirstCell();
				node = curRow.getLastCell();
			}
		}
		
	}

	public String getVarname() {
		return varname;
	}

	public void setVarname(String varname) {
		this.varname = varname;
	}

	public Node getBegin() {
		return begin;
	}

	public void setBegin(Node begin) {
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
		this.end = makeNormalNode(end);
		if (getEnd().getNodeType() == NodeType.CELL) 
			setInTable(true);
		else
			setInTable(false);
	}

	public CompositeNode<?> getFamily() {
		return (CompositeNode<?>) family;
	}

	public void setFamily(Node f) {
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

	public void expand(List<Node> inserted) throws Exception {
		
		if (!isInTable()) {
			for (Node tmp : nodelist) {
				Node newnode = tmp.deepClone(true);
				if (tmp != null)
					getFamily().insertAfter(newnode, getEnd());
				inserted.add(newnode);
			}
		} else { // in table
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
	}

}
