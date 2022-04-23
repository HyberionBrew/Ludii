package approaches.mkchain;

import java.util.ArrayList;
import java.util.List;

public class LudemeNode {
	private Class<?> Ludeme;
	private List<LudemeNode> children = new ArrayList<>();;
	private boolean isArray =false;
	private boolean isIterable = false;
	
	public LudemeNode(final Class<?> LudemeClass) {
		Ludeme = LudemeClass;
	}
	public LudemeNode(final Class <?> LudemeClass,Boolean isarr) {
		Ludeme = LudemeClass;
		isArray = isarr;
		isIterable = !isarr;
	}
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	public void addChild(LudemeNode node) {
		children.add(node);
	}
	public void addChild(LudemeNode node, Boolean isArray) {
		this.addChild(node);
		this.isArray = isArray;
		this.isIterable = !isArray;		
	}
	
	public List<LudemeNode> getChildren(){
		return children;
	}
	public String toString() {
		return Ludeme.getName();
	}
	/*@Override
	public String toString() {
		
		StringBuilder str = new StringBuilder();

		str.append(Ludeme.getName() + "----");
		for (LudemeNode child: children) {
			str.append(child.toString());
		}
		str.append("\n");
		return str.toString();
	}*/
	public boolean isArray() {
		return isArray;
	}
	public boolean isIterable() {
		return isIterable;
	}
}
