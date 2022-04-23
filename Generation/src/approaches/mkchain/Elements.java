package approaches.mkchain;

import java.util.ArrayList;
import java.util.List;

import main.grammar.Token;

public class Elements<T> {
		private List<T> classes = new ArrayList();
		private List<Elements> ArrayElements = new ArrayList();
		private String TokenName;
		private int count = 0;
		private boolean isArray = false;
		private boolean isTerminal = false;
		private Token token;
		
		public Token getToken() {
			return token;
		} 
		public List<Elements> getArrayElements(){
			return ArrayElements;
		}
		public List<T> getValues() {
			return classes;
		}
		public String getParameterLabel() {
			if (token==null) return null;
			return token.parameterLabel();
		}
		public String getName() {
			
			/*if (token!=null) {
				if (token.parameterLabel() != null) {
					System.out.println("---:"+ token.parameterLabel() +":"+ token.name());
					return token.parameterLabel() +":"+ token.name();
				}
			}*/
			return TokenName;
		}
		public boolean hasLabel() {
			if (token==null) {
				return false;
			}
			if (token.parameterLabel()!=null) {
				return true;
			}
			return false;
		}
		public boolean isArray() {
			return isArray;
		}
		public boolean isTerminal() {
			return isTerminal;
		}
		Elements(String TokenName, T TokenValue, Token token){
			classes.add(TokenValue);
			isTerminal = true;
			this.TokenName = TokenName;
			count = 1;
			this.token = token;
		}
		Elements(String TokenName, Token token){
			this.TokenName = TokenName;
			count = 1;
			this.token = token;
			if (TokenName.equals("<Terminal>")) isTerminal=true;
		}
		
		Elements(String TokenName, Token token, ArrayList<Elements> ElementsList){
			this.isArray = true;
			this.token = token;
			this.TokenName = TokenName;
			ArrayElements.addAll(ElementsList);
			count = 1;
		}
		
		/*void add(T TokenValue){
			classes.add(TokenValue);
			count ++;
		}*/
		void add(Elements<T> elem) {
			if (!(classes.isEmpty())) {
				for (T val: elem.classes) {
					this.classes.add(val);
					count ++;
				}
			}
			else {
				this.count = this.count + elem.getCount();
			}
		}
		void add() {
			count ++;
		}
		int getCount() {
			return count;
		}
		List<T> getTokenValues() {
			return classes;
		}
		public String toString(){
			String str = this.token.toString();
			if (isArray) {
				str = str + ": {";
				for (Elements el: ArrayElements)
				{
					str = str + el.toString()+ " ";
				}
				str = str + " }";
			}
			return str + " : "+ count;
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
		    Elements c = (Elements) o;
		    // field comparison
		    //compare each element if one does not fit they are not equal
		    //TODO! handle array ffs
		    if (isArray) {
		    	if (c.ArrayElements.size() != this.ArrayElements.size()) return false;
		    	for (int i= 0; i<c.ArrayElements.size(); i++) {
		    		if (!c.ArrayElements.get(i).equals(this.ArrayElements.get(i))) return false;
		    	}
		    }
		    return c.TokenName.equals(this.TokenName);
		}
		
	
}
