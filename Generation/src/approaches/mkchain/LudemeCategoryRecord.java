package approaches.mkchain;

import java.util.ArrayList;
import java.util.List;

public class LudemeCategoryRecord
{
	
	class childrenRecord {
		private final Class<?> childLudemeClass;
		private int count = 0;
		public childrenRecord(final Class<?> Ludeme)
		{
			this.childLudemeClass = Ludeme;
			increment();
		}
		public void increment() {
			count++;
		}
		public int getCount() {
			return count;
		}
		public Class<?> LudemeClass(){
			return childLudemeClass;
		}
		public String toString() {
			return this.childLudemeClass.getName()+ " * "+ count + " ";
		}
		
	}
	
	class LudemeRecord {
		private final Class<?> LudemeClass;
		private List<childrenRecord> children = new ArrayList<>();
		public LudemeRecord(final Class<?> category)
		{
			this.LudemeClass = category;
		}
		public Class<?> LudemeClass(){
			return LudemeClass;
		}
		
		public List<Integer> getChildCount() {
			List<Integer> list=new ArrayList<Integer>();
			for (childrenRecord child : children) {
				list.add(child.getCount());
			}
			return list;
		}
		
		public List<Class<?>> getChildClasses() {
			List<Class<?>> list=new ArrayList<>();
			for (childrenRecord child : children) {
				list.add(child.LudemeClass());
			}
			return list;
		}
		
		public boolean updateChildren(Class<?> Ludeme, Class<?> ChildLudeme) {
			//check if there is child ludeme already in the count record
			assert(Ludeme.equals(LudemeClass));
			boolean contained = false;
			for (childrenRecord child: children) {
				if (child.LudemeClass().equals(ChildLudeme)) {
					System.out.println("Found");
					contained = true;
					child.increment();
					break;
				}
			}
			if (!contained) {
				System.out.println(ChildLudeme);
				children.add(new childrenRecord(ChildLudeme));
			}
			return true;
		}
		public boolean containsLudeme(Class<?> LudemeToCheck) {
			for (childrenRecord child : children) {
				if (child.LudemeClass().equals(LudemeToCheck)) return true;
			}
			return false;
		}
		public boolean hasChildren() {
			return !children.isEmpty();
		}
		public String toString() {
			StringBuilder str = new StringBuilder();
 
			str.append(LudemeClass.getName());
			if (this.hasChildren()) {
				str.append(": Children: |");
			}
			for (childrenRecord child: children) {
				str.append(child.toString()+"|");
			} 
			return str.toString();
			
		}
		//expand for auxilliary information in case of functions or smth. or array
	}
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (LudemeRecord record: assignedClasses) {
			str.append(record.toString()+"\n");
		}
		return str.toString();
	}
	private final String label;
	private final Class<?> category;
	// modification
	private List<LudemeRecord> assignedClasses = new ArrayList<>();
	private int count = 0;

	//-------------------------------------

	public LudemeCategoryRecord(final String label, final Class<?> category)
	{
		this.label = label;
		this.category = category;
	}
	
	
	//-------------------------------------
	
	public String label()
	{
		return label;
	}

	public Class<?> category()
	{
		return category;
	}

	public int count()
	{
		return count;
	}

	public void reset()
	{
		assignedClasses = new ArrayList<>();
		count = 0;
	}
	public List<LudemeRecord> getElements(){
		return assignedClasses;
	}
	public void addElement(Class<?> ClassToAdd) {
		assignedClasses.add(new LudemeRecord(ClassToAdd));
	}
	public void increment()
	{
		count++;
	}
	public boolean containsLudeme(Class<?> LudemeToCheck) {
		for (LudemeRecord ludem: assignedClasses) {
			if (ludem.LudemeClass().equals(LudemeToCheck)) return true;
		}
		return false;
	}
	public boolean updateChildren(Class<?> Ludeme, Class<?> ChildLudeme) {
		// could just not use this
		if (!containsLudeme(Ludeme)) {
			System.out.println("The parent is not contained");
			return false;
		}
		
		// update the child of assignedClasses
		// loop through all contained ludemes and 
		for (LudemeRecord ludem: assignedClasses) {
			if (ludem.LudemeClass().equals(Ludeme)) {
				// we are at parent ludem update its childrens
				ludem.updateChildren(Ludeme, ChildLudeme);
				return true;
			}
			
		}
		//something went wrong
		assert(true);
		return false;
	}
	
	public void printChildren() {
		for (LudemeRecord ludeme: assignedClasses) {
			if (ludeme.hasChildren()) {
				System.out.println(ludeme.toString());
			}
		}
	}
		
	

}
