package approaches.mkchain;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import approaches.mkchain.LudemeCategoryRecord.LudemeRecord;
import grammar.ClassEnumerator;
//import supplementary.experiments.ludemes.CountLudemes.Record;

/**
 * Record of a ludeme class category and number of times it occurs.
 */
public class LudemeChainBuilder {
	/**
	 * Add ludemes categories to be counted here.
	 */
	void prepareCategories()
	{
		records.clear();
		
		try
		{	
			records.add(new LudemeCategoryRecord("Ludeme classes",      Class.forName("other.Ludeme")));
			records.add(new LudemeCategoryRecord("Integer functions",   Class.forName("game.functions.ints.BaseIntFunction")));
			records.add(new LudemeCategoryRecord("Boolean functions",   Class.forName("game.functions.booleans.BaseBooleanFunction")));	
			records.add(new LudemeCategoryRecord("Region functions",    Class.forName("game.functions.region.RegionFunction")));	
			records.add(new LudemeCategoryRecord("Equipment (total)",   Class.forName("game.equipment.Item")));
			records.add(new LudemeCategoryRecord("Containers",          Class.forName("game.equipment.container.Container")));
			records.add(new LudemeCategoryRecord("Components",          Class.forName("game.equipment.component.Component")));
			records.add(new LudemeCategoryRecord("Start rules",         Class.forName("game.rules.start.StartRule")));
			records.add(new LudemeCategoryRecord("Moves rules",         Class.forName("game.rules.play.moves.Moves")));
			records.add(new LudemeCategoryRecord("End rules",           Class.forName("game.rules.end.End")));		
			//records.add(new Record("Game type modifiers", Class.forName("game.types.GameType")));						
			//records.add(new Record("", Class.forName("game.")));
		}
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public List<LudemeCategoryRecord> CreateList() 
	{
		prepareCategories();
		return createLudemeList();
	}
	
	//---------------------------------------------------------
	
	private final List<LudemeCategoryRecord> records = new ArrayList<LudemeCategoryRecord>();
	
	//adapted from package supplementary.experiments.ludemes; 
	// CountLudemes.java
	private List<LudemeCategoryRecord> createLudemeList() {
		final String rootPackage = "game.Game";
		Class<?> clsRoot = null;
		
		try
		{
			clsRoot = Class.forName(rootPackage);
		} 
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
			System.out.println("Couldn't find root package \"game\".");
			return null;
		}
		final List<Class<?>> classes = ClassEnumerator.getClassesForPackage(clsRoot.getPackage());
		List<Class<?>> fixed = new ArrayList<>();
		// Get the reference Ludeme class
		Class<?> clsLudeme = null;
		try
		{
			clsLudeme = Class.forName("other.Ludeme");
		}
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		// Count ludemes in each category
		int numEnums = 0;
		int numEnumConstants = 0;
		for (final Class<?> cls : classes)
		{
			for (final LudemeCategoryRecord record : records)
			{
				if 
				(
					clsLudeme.isAssignableFrom(cls)
					&&
					record.category().isAssignableFrom(cls)
					&&
					!Modifier.isAbstract(cls.getModifiers())
					&&
					!cls.getName().contains("$")
				)
				{
					// Is a ludeme class
					record.increment();
					record.addElement(cls);
					//System.out.println(record.label());
					//System.out.println(cls.getName());
					
				}
			}
			
			if (cls.isEnum())
			{
				// Is an enum class
				numEnums++;
				numEnumConstants += cls.getEnumConstants().length;
			}
		}
		/*for (final LudemeCategoryRecord record : records)
		{
			System.out.println(record.label());
			for (final LudemeRecord elem: record.getElements()) {
				System.out.println(elem.getName());
			}
		}*/
		return records;
	}
}
