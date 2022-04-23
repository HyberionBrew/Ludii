package approaches.mkchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import approaches.mkchain.LudemeCategoryRecord.LudemeRecord;
import main.grammar.Token;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Continuation {
	public List<Elements> getElements() {
		return continuationElements;
	}
	private List<Elements> continuationElements = new ArrayList();
	Continuation(List<Token> arguments){
		if (arguments.isEmpty()) {
			continuationElements.add(new Elements("<Terminal>",new Token()));
		}
		for (Token arg: arguments) {
			// in those cases we want to append a list of the encountered values
			Elements newElement = buildElements(arg);		
			continuationElements.add(newElement);
		}
		//System.out.println(continuationElements);
	}
	public int getCount() {
		if (continuationElements.isEmpty()) return 0;
		return continuationElements.get(0).getCount();
	}
	private Elements buildElements(Token token) {
		// snowflake array
		if (token.isArray()) {
			//System.out.println(token.parameterLabel());
			ArrayList<Elements> values = new ArrayList();
			for (Token t: token.arguments()) {
				Elements el = buildElements(t);
				//Elements el = processToken(t);
				values.add(el);
			}
			return new Elements("<Array>",new Token(),values);
		}
		return processToken(token);

	} 
	
	private Elements processToken(Token token) {
		// array cannot be terminal
		if (token.isTerminal()) {
			assert(!token.isArray());
			boolean isString = token.name().substring(0,1).equals("\"");
			if (isString) {
				return new Elements<String>("<string>",token.name(),token.TokenNoChildren());
			}
			// check if int
			// regular expression for an integer number
			String regexInt = "[+-]?[0-9]+";
			Pattern p = Pattern.compile(regexInt);
			Matcher m = p.matcher(token.name());
			if(m.find() && m.group().equals(token.name())) {
				return new Elements<Integer>("<int>",Integer.parseInt(token.name()), token.TokenNoChildren());
			}
			// chech if float
			String regexFloat = "[+-]?[0-9]+(\\.[0-9]+)?([Ee][+-]?[0-9]+)?";
			p = Pattern.compile(regexFloat);
			m = p.matcher(token.name());
			if(m.find() && m.group().equals(token.name())) {
				return new Elements<Float>("<float>",Float.parseFloat(token.name()), token.TokenNoChildren());
			}
			
			//check if boolean
			if (token.name().matches("[Tt]rue|[Ff]alse") && Boolean.parseBoolean(token.name())) {
				return new Elements<Boolean>("<bool>",Boolean.parseBoolean(token.name()), token.TokenNoChildren() );
			}
		} 
		return new Elements<String>(token.name(), token.TokenNoChildren());
		
		
	}
	@Override
	public boolean equals(Object o) {
	    // self check
	    if (this == o)
	        return true;
	    // null check
	    if (o == null)
	        return false;
	    // type check and cast
	    if (getClass() != o.getClass())
	        return false;
	    Continuation c = (Continuation) o;
	    // field comparison
	    //compare each element if one does not fit they are not equal
	    //chech if same length
	    if (!(c.continuationElements.size() == this.continuationElements.size())) {
	    	return false;
	    }
	    for (int i=0; i< c.continuationElements.size(); i++) {
	    	if (!(c.continuationElements.get(i).equals(this.continuationElements.get(i)))) 
	    		return false;
	    }
	    return true;
	}


	public Boolean expand(Continuation cont) {
		// make sure its equal
		if (!this.equals(cont)){
			return false;
		}
		// add to each but also append arrays
		for (int i= 0; i<this.continuationElements.size(); i++) {
			this.continuationElements.get(i).add(cont.continuationElements.get(i));
		}
		return true;
	}
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[");
		for (Elements el: continuationElements) {
			str.append(el.toString() + " ");
			if (!el.getValues().isEmpty()) {
				str.append("{ ");
				str.append(el.getTokenValues());
				
				str.append(" }");
			}
			
		}
		str.append("]");
		return str.toString(); 
	}
}