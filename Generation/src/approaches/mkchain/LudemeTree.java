package approaches.mkchain;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;

import game.Game;
import main.ReflectionUtils;
import other.Ludeme;

//TODO! build workign ludeme game back from tree
public class LudemeTree {
	private LudemeNode root = null;
	public LudemeTree(final Game game){
		//root = new LudemeNode(game.getClass());
		root = build(game);	
	}
	public LudemeTree(final Ludeme ludeme) {
		root = new LudemeNode(ludeme.getClass());
	}
	
	public LudemeNode getRoot() {
		return root;
	}
	
	public void print() {
		System.out.println(root);
	}
	
	public String toString() {
		return root.toString();
	}
	
	
	private LudemeNode build(Ludeme ludeme) {
		final Class<? extends Ludeme> clazz = ludeme.getClass();
		final List<Field> fields = ReflectionUtils.getAllFields(clazz);
		System.out.println(fields);
		LudemeNode expandedTree = new LudemeNode(ludeme.getClass()); //set tree to current node
		try {
			for (final Field field : fields)
			{
				field.setAccessible(true);
				
				if ((field.getModifiers() & Modifier.STATIC) != 0)
				{
					continue;
				}
								
				final Object value = field.get(ludeme);
				
				if (value != null)
				{
					final Class<?> valueClass = value.getClass();
					
					if (Ludeme.class.isAssignableFrom(valueClass))
					{
						// We've found a ludeme!
						//ludemeCounts.adjustOrPutValue(valueClass.getName(), 1, 1);
						System.out.println(value.getClass().getName());
						
						expandedTree.addChild(build((Ludeme) value));
					}
					else if (valueClass.isArray())
					{
						final Object[] array = ReflectionUtils.castArray(value);
						//LudemeNode arrayTree = new LudemeNode(valueClass, true); 
						for (final Object element : array)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									
									System.out.println("2" + value.getClass().getName());
									expandedTree.addChild(build((Ludeme) element), true);
									//ludemeCounts.adjustOrPutValue(element.getClass().getName(), 1, 1);
									//updateLudemeCounts(ludemeCounts, (Ludeme) element, visited);
								}
							}
						}
						
						//System.out.println("xxx" + arrayTree);
						//if (arrayTree.hasChildren()) expandedTree.addChild(arrayTree);
					}
					else if (Iterable.class.isAssignableFrom(valueClass))
					{
						final Iterable<?> iterable = (Iterable<?>) value;
						//LudemeNode iterableTree = new LudemeNode(valueClass, false); 
						for (final Object element : iterable)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									System.out.println("3" + value.getClass().getName());
									expandedTree.addChild(build((Ludeme) element));
									//ludemeCounts.adjustOrPutValue(element.getClass().getName(), 1, 1);
									//updateLudemeCounts(ludemeCounts, (Ludeme) element, visited);
								}
							}
						}
						//expandedTree.addChild(iterableTree);
					}
				}
			}
		}
		catch (final IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return expandedTree;
	}
	
}
