package approaches.mkchain;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import grammar.Grammar;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Token;
import main.grammar.ebnf.EBNF;
import main.grammar.ebnf.EBNFRule;
import main.options.UserSelections;
import other.GameLoader;
import parser.Parser;

import java.util.Random;

import approaches.mkchain.*;
import approaches.random.Generator;
import compiler.Compiler;

public class MkChainGenerator {
	/**
	 * @return Returns list of rules that match a given name.
	 */
	static List<EBNFRule> findRules(final String ruleName) {
		final List<EBNFRule> list = new ArrayList<EBNFRule>();

		final EBNF ebnf = Grammar.grammar().ebnf();

		String str = StringRoutines.toDromedaryCase(ruleName);

		// Try exact match
		EBNFRule rule = ebnf.rules().get(str);
		if (rule != null) {
			list.add(rule);
			return list;
		}

		// Try match with rule delimiters '<...>'
		rule = ebnf.rules().get("<" + str + ">");
		if (rule != null) {
			list.add(rule);
			return list;
		}

		// Find any match
		if (str.charAt(0) == '<')
			str = str.substring(1);
		if (str.charAt(str.length() - 1) == '>')
			str = str.substring(0, str.length() - 1);

		for (final EBNFRule ruleN : ebnf.rules().values()) {
			String strN = ruleN.lhs();
			if (strN.charAt(0) == '<')
				strN = strN.substring(1);
			if (strN.charAt(strN.length() - 1) == '>')
				strN = strN.substring(0, strN.length() - 1);

			while (strN.contains(".")) {
				final int c = strN.indexOf(".");
				strN = strN.substring(c + 1);
			}

			if (strN.equalsIgnoreCase(str))
				list.add(ruleN);
		}

		return list;
	}

	private DeconstructTree proDist = null;

	public MkChainGenerator(DeconstructTree dec) {
		proDist = dec;
	}
	public MkChainGenerator() {
	}

	final Report report = new Report();

	public Token generateGame() {
		Token n = new Token("(game)", null);
		Token game = complete(n);
		return game;
	}
	
	public Game testGame(String str) {
		final boolean containsPlayRules = str.contains("(play");
		final boolean containsEndRules = str.contains("(end");
		final boolean containsMatch = str.contains("(match");
		Generator gen = new Generator();
		if (!containsPlayRules || !containsEndRules || containsMatch) {
			System.out.println("Game is a match or does not generate a play and end rule");
			// + numTry);
			return null;
		}
		// Check whether game parses
		final Description description = new Description(str);
		final UserSelections userSelections = new UserSelections(new ArrayList<String>());

		Parser.expandAndParse(description, userSelections, report, false);
		if (report.isError()) {
			// Game does not parse
			// FileHandling.saveStringToFile(str,
			// "../Common/res/lud/test/buggy/unparsable/", fileName);

			
			System.out.println("Game unparsable for try ");
			return null ;
		}

//					for (final String warning : report.warnings())
//						if (!warning.contains("No version info."))
//							System.out.println("- Warning: " + warning);

		// Check whether game compiles
		Game game = null;
		try {
			game = (Game) Compiler.compileTest(new Description(str), false);
		} catch (final Exception e) {
			// Nothing to do.
		}

		if (game == null) {
			// Game does not compile
			System.out.println("Game uncompilable for try " );
			return null;
		}

		if (game.hasMissingRequirement() || game.willCrash()) {
			System.out.println("Game with warning and possible crash for try " );
			return null;
		}

		// No boardless, No match, no deduc Puzzle, cards, dominoes or large pieces, no
		// hidden info.
		// Only Alternating game.
		if (true)
			if (game.hasSubgames() || game.isBoardless() || game.isDeductionPuzzle() || game.hasCard()
					|| game.hasDominoes() || game.hasLargePiece() || !game.isAlternatingMoveGame()
					|| game.hiddenInformation()) {
				
				System.out.println("Game not satisfying for try ");
				return null;
			}

		final String fileName = game.name() + ".lud";

		// Check whether game is functional
		if (false) {
			if (!gen.isFunctionalAndWithOnlyDecision(game)) {
				// Game is not functional
				
				System.out.println("Game non functional or has a move with no decision for try");
				return null ;
			}
		} else if (!gen.isFunctional(game)) {
			// Game is not functional
	
			//FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/nonfunctional/", fileName);
			System.out.println("Game non functional");
			return null;
		}

		// Check whether game is playable
		if (!gen.isPlayable(game)) {
			// Game is not playable
		
			//FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/unplayable/", fileName);
			System.out.println("Game unplayable");
			return null;
		} else {
			//FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/toTest/", fileName);
			System.out.println("GAME GENERATED everything nice");
			return game;
		}

	}

		
	private Token complete(Token token) {
		// for now do not sample from all possible rules
		// first scan prop. distr. for the token name
		TokenDescriptor desc;
		/*
		 * try { desc = proDist.find(token); } catch (Exception e) {
		 * e.printStackTrace(); return new Token(" ", null); //return null; }
		 */
		desc = proDist.find(token);
		if (desc == null) {
			//System.out.println(token.toString());
			return token; //new Token(token.name(), null);
		}
		// now we know how to continue, sample from it
		ArrayList<Integer> propbs = desc.getCounts();
		//System.out.println(desc.getCounts());
		int sum = 0;
		for (int p : propbs) {
			sum += p;
		}
		Random rand = new Random();
		//rand.setSeed(42);
		// Generate random integers in range 1 to sum-1
		int r = rand.nextInt(sum);
		// now yield one rule
		int selected = 0;
		sum = 0;
		for (int p : propbs) {
			sum += p;
			if (r <= sum) {
				break;
			}
			selected++;
		}
		Continuation pick = desc.getContinuations().get(selected);
		// potentially add a fully random pick based on rule e.g. 5%
		//System.out.println(pick);
		// convert pick into tokens that are appended to the current token
		List<Elements> ContTs = pick.getElements();
		//System.out.println("Entering");
		for (Elements e : ContTs) {
			if (e.isTerminal()) {
				if (e.getValues().isEmpty()) {
					token.arguments(true).add(new Token());
				}
				else {
					// currently just picks the first element should be randomized
					String value = e.getValues().get(0).toString();
					if (value.equals("true")){
						value = "True";
					}
					if (value.equals("false")){
						value = "False";
					}
					
					token.arguments(true).add(new Token(value, null));
					// have to make sure that if it is a boolean it is uppercase
				}
			} else if (e.isArray()) {
				token = buildArrayToken(token,e);
			} else {
				// e is class, maybe add that no () is added in case of terminal or smth?
				if (e.hasLabel()){
					Token temp = complete(new Token( "(" +e.getName()+ ")", null));
					temp.setParameterLabel(e.getParameterLabel());
					token.arguments(true).add(temp);
				}
				else {
					token.arguments(true).add(complete(new Token("(" + e.getName() + ")", null)));
				}
			}
			//System.out.println("Current:" + token);
		}
		// sum up
		//System.out.println(token);
		return token;
	}
	
	private Token buildArrayToken(Token token, Elements e) {
		Token tokenArray = new Token("{}", null);
		List<Elements> arr = e.getArrayElements();
		for (Elements ee : arr) {
			// problematic for deeper arrays
			if (ee.isArray()) {
				Token deepArray = buildArrayToken(new Token(), ee);
				tokenArray.arguments(true).add(deepArray);
			}
			else {
				tokenArray.arguments(true).add(complete(new Token("(" + ee.getName() + ")", null)));
			}
		}
		token.arguments(true).add(tokenArray);
		return token;
	}


	public static void main(final String[] arg)
	{
		

		DeconstructTree dec = new DeconstructTree();
		
		final String[] allGames = FileHandling.listGames();
		
		
		// Load (some)
		int DeconstructGames = 100;
		int ii = 0;
		for (String g: allGames) {
			String[] arrOfStr = g.split("/");
			if (arrOfStr[2].equals("board")) {
				// now check for the concepts 
				Game game = GameLoader.loadGameFromName(arrOfStr[arrOfStr.length-1]);
				if (game.booleanConcepts().get(23) && game.booleanConcepts().get(6) && !game.hasSubgames()) {
					System.out.println("adding: "+game.name()); // 23 -> TwoPlayers | 7 -> Alternating Moves 
					//games.add(game);
					dec.processTokenTree(game.description().tokenForest().tokenTree());
					ii++;
				}
				if (ii == DeconstructGames) {
					// for small scale tests
					break;
				}			
			}
		}
		
		final MkChainGenerator gen = new MkChainGenerator(dec);
		// now generate 10 games
		int validGames = 0;
		int gamestoGenerate = 10000;
		for (int i=0;i<gamestoGenerate;i++) {
			Token GeneratedGame = gen.generateGame();
			//System.out.println(GeneratedGame.toString());
			Game gg = gen.testGame(GeneratedGame.toString());
			if (gg != null) {
				validGames++;
				System.out.println("Valid Game generated!");
				System.out.println(gg.description().tokenForest().tokenTree().toString());
			}
		}
		System.out.println("Generated "+ validGames + " valid Games! ("+gamestoGenerate+") tries");
	
	}
	
	
	
	

}
