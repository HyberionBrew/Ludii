package approaches.mkchain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import grammar.Grammar;
import main.EditorHelpData;
import main.grammar.LudemeInfo;
import main.grammar.Token;

public class DeconstructTree {
	public DeconstructTree() {
		
	}
	public DeconstructTree(Token rootToken) {
		
		//Set<String> knowLudemes = preprocessKnownLudemes();
		processTokenTree(rootToken);
	}
	public void processTokenTree(Token token) {
		
		addToTokenTree(token);
		// if token is array stub do not spawn new exploration from it
		// actual exploration
		for (Token tok: token.arguments()) {
			if (!tok.isTerminal()) { // && !(tok.isArray())
				processTokenTree(tok);
			}
			// if it is an array we skip it and spawn underlying
		}
	}
	
	
	private void addToTokenTree(Token token) {
		if (!token.isTerminal()) {
			boolean found = false;
			for (TokenDescriptor tokenDesc: TokenDescriptorList) {
				String TokenString = token.name();
				if (token.isArray()){
					TokenString = "<Array>";
				}
				if (tokenDesc.parentName().equals(TokenString)) {
					tokenDesc.expand(token.arguments());
					found = true;
				}
			}
			if (!found) {
				TokenDescriptorList.add(new TokenDescriptor(token,token.arguments()));
			}
		}
	}
	
	
	private Set<String> preprocessKnownLudemes() {
		final List<LudemeInfo> ludemes = Grammar.grammar().ludemesUsed(); 
		System.out.println(ludemes.size() + " ludemes loaded.");

		int idCounter = 1;
		
		// Get JavaDoc help for known ludemes
		final EditorHelpData help = EditorHelpData.get();
		Set<String> set = new HashSet<>();
		for (final LudemeInfo ludeme : ludemes)
		{
			set.add(ludeme.symbol().token()); // No duplicates
		}
		return set;
	}
	public String toString() {
		String str = "Deconstructor";
		for (TokenDescriptor cont: TokenDescriptorList) {
			str = str + "    " +cont.toString() + "\n";
		}
		return str;
	}
	private ArrayList<TokenDescriptor> TokenDescriptorList = new ArrayList<TokenDescriptor>();
	
	public ArrayList<TokenDescriptor> getTokenDescriptors (){
		return TokenDescriptorList;
	}
	public TokenDescriptor find(Token token) { //throws Exception {
		for (TokenDescriptor desc: TokenDescriptorList) {
			//System.out.println(token.name());
			if (token.name().equals(desc.parentName())) {
				return desc;
			}
		}
		//throw new Exception("Does not contain the token?!:" + token.name());
		return null;
	}

}
