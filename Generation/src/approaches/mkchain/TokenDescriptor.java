package approaches.mkchain;

import java.util.ArrayList;
import java.util.List;

import main.grammar.Token;

public class TokenDescriptor {
	//List<Elements> arguments = new ArrayList<Elements>(); 
	private Token parentToken;
	private List<Continuation> possibleContinuations= new ArrayList<Continuation>();
	private String tokenName = "";
	public List<Continuation> getContinuations(){
		return possibleContinuations;
	}
	public TokenDescriptor(Token token, List<Token> arguments) {
		parentToken = token;
		tokenName = token.name();
		if (token.name() == null) {
			if (token.isArray()) {
				tokenName = "<Array>";
			}
		}
		assert(tokenName!=null);
		possibleContinuations.add(new Continuation(arguments));
	}

	public String parentName() {
		return tokenName;
	}
	
	public ArrayList<Integer> getCounts() {
		ArrayList<Integer> ar = new ArrayList<Integer>();
		for (Continuation c:possibleContinuations) {
			ar.add(c.getCount());
		}
		return ar;
	}

	public void expand(List<Token> arguments) {
		//search if continuation exists, if the case add to it
		boolean continuationExists= false;
		//check if arguments are an array, if thats the case expand
		Continuation addC = new Continuation(arguments);
		for (Continuation cont: possibleContinuations) {
			
			if (cont.equals(addC)) {
				cont.expand(addC);
				continuationExists=true;
			}
		}
		if (!continuationExists) {
			possibleContinuations.add(addC);
		}
	}
	
	public String toString() {
		String str = this.parentName()+ " (parent) \n";
		for (Continuation cont: possibleContinuations) {
			str = str + "    " +cont.toString() + "\n";
		}
		return str;
	}

}
