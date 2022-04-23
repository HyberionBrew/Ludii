package approaches.evolution;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import approaches.mkchain.MkChainGenerator;
import game.Game;
import main.FileHandling;
//import Random;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Token;
import main.options.Ruleset;
import other.GameLoader;
import supplementary.experiments.eval.*;
import metrics.Evaluation;
import metrics.Metric;

public class Generator {
	private class TokenPosition {
		public Token getToken() {
			return token;
		}
		
		public List<Integer> getPosition() {
			return position;
		}

		private Token token;
		private List<Integer> position = new ArrayList();
		TokenPosition(Token token,List<Integer> position){
			this.token = token;
			this.position = position;
		}
		
		public String toString() {
			return token.parameterLabel() + " " + token.name() + " " + position ;
		}
		
	}
	public List<TokenPosition> getNonTerminals(Game game) {
		final Description rulesets = game.description(); 
		Token token = rulesets.tokenForest().tokenTree();
		//System.out.println(token);
		List<TokenPosition> allTokens = recursiveTraversal(token,new ArrayList());
		return allTokens;
	} 
	private List<TokenPosition> recursiveTraversal(Token token, List<Integer> position) {
		List<Token> args = token.arguments();
		List<TokenPosition> allTokens = new ArrayList<TokenPosition>();
		if (!args.isEmpty()) {
			int i = 0;
			for (Token arg:args) {
				List<Integer> newList = new ArrayList();
				newList.addAll(position);
				newList.add(i);
				allTokens.addAll(recursiveTraversal(arg, newList)); //
				i++;
			}
			if (token.name()!=null) {
			if (!token.name().equals("game")) {
				allTokens.add(new TokenPosition(token,position));
			}
			}
			else {
				allTokens.add(new TokenPosition(token,position));
			}
		}
		return allTokens;
	}
	
	private static Random rnd = new Random(42);
	
	// list all non terminal tokens which are not arrays
	// pick second game and list all non terminal tokens which are not arrays
	// cross over if list is not empty
	// evaluate new game
	private static boolean tokensEquivalent(Token t1, Token t2) {
		
		if ((t1.isArray() != t2.isArray())) {
			return false;
		}
		if ((t1.isClass() != t2.isClass())){
			return false;
		}
		if ((t1.isTerminal() != t2.isTerminal())){
			return false;
		}
		
		if ((t1.parameterLabel() != null) && (t2.parameterLabel() != null)) {
			if (t1.parameterLabel().equals(t2.parameterLabel()))
			{
				return false;
			}
		}
		else if (t1.parameterLabel() == null && t2.parameterLabel() != null) {
			return false;
		}
		else if (t1.parameterLabel() != null && t2.parameterLabel() == null) {
			return false;
		}
		if (t1.name() == null && t2.name() == null) return true;
		if (t1.name() == null || t2.name()==null) return false;
		if (!(t1.name().equals(t2.name()))){
			return false;
		}
		return true;
	}
	
	// replaces the token tree in game at the modToken positon with the replacmeent Token
	public Token modifyGame(Token game, TokenPosition modToken, TokenPosition replacementToken) {
		Token currToken = game;
		//System.out.println(modToken);
		//System.out.println(replacementToken);
		if (modToken.getPosition().isEmpty()) {
			System.out.println("wants to replace game!");
			return replacementToken.getToken();
		}
		int i = 0;
		for (; i< modToken.getPosition().size()-1; i++) {
			//System.out.println(currToken.name()+ " i: "+ i);
			currToken = currToken.arguments().get(modToken.getPosition().get(i));
		}

		currToken.arguments(true).set(modToken.getPosition().get(i), replacementToken.getToken());

		return game;
	}
	
	public ArrayList<Double> evaluateGames(ArrayList<Game> games){
		ArrayList<Double> scores = new ArrayList<>();
		System.out.println("Evaluation started");
		int i = 0;
		for (Game g: games) {
			System.out.println("Game "+ i++);
			scores.add(evaluateGame(g, new Report(), 8, 50, 0.01, "Ludii AI", true));
		}
		return scores;
	}
	
	public double evaluateGame(Game game, final Report report, final int numberTrials, final int maxTurns, final double thinkTime, 
			final String AIName, final boolean useDBGames) {
		EvalGames ev = new EvalGames();
		//seupt as in EvalGames.java
		final Evaluation evaluation = new Evaluation();
		final List<Metric> metrics = evaluation.conceptMetrics();
		final ArrayList<Double> weights = new ArrayList<>();
		for (int i = 0; i < metrics.size(); i++)
			weights.add(Double.valueOf(0));
		weights.set(6,Double.valueOf(10));
		weights.set(8,Double.valueOf(-1));
		weights.set(9,Double.valueOf(1));
		weights.set(10,Double.valueOf(1));
		//weights.set(11,Double.valueOf(1));
		weights.set(12,Double.valueOf(0.1));
		weights.set(20,Double.valueOf(-10)); // drawishnes
		weights.set(22,Double.valueOf(3));
		weights.set(18,Double.valueOf(1));
		weights.set(19,Double.valueOf(1));
		weights.set(66,Double.valueOf(0.2));
		weights.set(77,Double.valueOf(0.2));
		
		final List<Ruleset> rulesets = game.description().rulesets();
		String outputString = "GameName,";
		
		System.setOut(dummyStream); // supress output from evaluateGame
		if (game.hasSubgames()) // TODO, we don't currently support matches
			return 0.;
		if (rulesets != null && !rulesets.isEmpty())
		{
			// jus calc for the first ruleset
			int rs = 0;
			if (!rulesets.get(rs).optionSettings().isEmpty()) {
				try {
				outputString += ev.evaluateGame(evaluation, report, game, rulesets.get(rs).optionSettings(), AIName, numberTrials, thinkTime, maxTurns, metrics, weights, useDBGames);
			}
				catch(Exception e){
					outputString = "0,0,0";
				}
			}
		}
		else {
			try {
			outputString += ev.evaluateGame(evaluation, report, game, game.description().gameOptions().allOptionStrings(game.getOptions()), AIName, numberTrials, thinkTime, maxTurns, metrics, weights, useDBGames);
			}
			catch(Exception e){
				outputString = "0,0,0";
			}
		}
		System.setOut(originalStream); // reenable
		//System.out.println(outputString);
		//int i = outputString.indexOf("Final Score: ");
		//String parsing = outputString.substring(i, i+10);
		String[] s = outputString.split(",");
		double ret = 0.0;
		try {
			ret = Double.parseDouble(s[1]);
		}
		catch(Exception e){
			ret = 0.0;
		}
		return ret;
	}
	
	public ArrayList<Game> crossGamesRandom(Genepool genepool, int crossings) {
		
		//int i = 0;
		ArrayList<Game> newGames = new ArrayList();
		for (int i = 0; i < crossings; i++) {
		//while (newGames.size() < crossings) {
			//int gameToCross1 = rnd.nextInt(inputGames.size());
			//int gameToCross2 = rnd.nextInt(inputGames.size());
			//draw depending on fitness
			int gameToCross1 = 0;
			int gameToCross2 = 0;
			while (gameToCross1 == gameToCross2) {
				gameToCross1 = genepool.random_draw();
				gameToCross2 = genepool.random_draw();
			}
			List<TokenPosition> allTokens1 = getNonTerminals(genepool.getGame(gameToCross1));
			List<TokenPosition> allTokens2 = getNonTerminals(genepool.getGame(gameToCross2));
			List <TokenPosition> commonTokens1 = new ArrayList();
			List <TokenPosition> commonTokens2 = new ArrayList();
			for (TokenPosition tp1 : allTokens1) {
				for (TokenPosition tp2 : allTokens2) {
					//System.out.println("Testing "+ tp1 + " tp2: " + tp2);
					if (tokensEquivalent(tp1.getToken(), tp2.getToken())) {
						commonTokens1.add(tp1);
						commonTokens2.add(tp2);
						//System.out.println(tp1);
						//System.out.println(commonTokens2.size());
					}
				}
			}
			//System.out.println(commonTokens1.size()+ " : overlap in non.Terminal tokens");
			//System.out.println(commonTokens1);
			int position = rnd.nextInt(commonTokens1.size());
			//System.out.println("Pos: "+ position);
			TokenPosition drawnToken = commonTokens1.get(position);
			TokenPosition replacementToken= commonTokens2.get(position);
		
			final Description rulesets = genepool.getGame(gameToCross1).description(); 
			Token orgGame = rulesets.tokenForest().tokenTree().clone();
			//System.out.println(orgGame.name());
			//System.out.println(orgGame);
			//System.out.println(drawnToken);
			//System.out.println(replacementToken.getToken());
			Token newGame = modifyGame(orgGame,drawnToken, replacementToken);
			MkChainGenerator mk  = new MkChainGenerator();
			//System.out.println(newGame);
			//Token token = rulesets.tokenForest().tokenTree();
			//mk.testGame(inputGames.get(1).description().tokenForest().tokenTree().toString());
			i++;
			try {
				System.setOut(dummyStream); 
				Game validGame = mk.testGame(newGame.toString());
				System.setOut(originalStream); 
				if (validGame != null) {
					newGames.add(validGame);
				}
			}catch (Exception e){
				//System.out.println("Game not working");
			}
			// now also evaluate how fun the game is
			if (newGames.size()%10 == 0) {
				System.out.println("Current working: " + newGames.size() +"/" + i);
			}
			//break;
		}

		
		return newGames;
	}
	
	public ArrayList<Game> createInitialPopulation(int size, boolean all) {
		final String[] allGames = FileHandling.listGames();
		ArrayList<Game> games = new ArrayList();
		
		for (String g: allGames) {
			String[] arrOfStr = g.split("/");
			if (arrOfStr[2].equals("board")) {
				// now check for the concepts 
				Game game = GameLoader.loadGameFromName(arrOfStr[arrOfStr.length-1]);
				if (game.booleanConcepts().get(23) && game.booleanConcepts().get(6) && !game.hasSubgames()) {
					System.out.println("adding: "+game.name()); // 23 -> TwoPlayers | 7 -> Alternating Moves 
					games.add(game);
				}
				if (games.size() == size && !all) {
					// for small scale tests
					break;
				}			
			}
		}
		return games;
	}
	private PrintStream originalStream = System.out;
	private PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});
	
	class Genepool {
		private ArrayList<Game> games = new ArrayList();
		private ArrayList<Double> fitness = new ArrayList();
		private ArrayList<Boolean> generated = new ArrayList();
		Genepool(){
			
		}
		public void add(ArrayList<Game>games, ArrayList<Double> fitness, boolean generated){
			this.games.addAll(games);
			this.fitness.addAll(fitness);
			for (int i=0; i< games.size();i++)
				this.generated.add(generated);
		}
		public int random_draw() {
			// function that randomly draws weighted by the fitness
			int sum = 0;
			for (double p : fitness) {
				sum += p;
			}
			Random rand = new Random();
			//rand.setSeed(42);
			// Generate random integers in range 0 to sum
			double r = rand.nextDouble(sum);
			// now yield one rule
			int selected = 0;
			sum = 0;
			for (double p : fitness) {
				sum += p;
				if (r <= sum) {
					break;
				}
				selected++;
			}
			return selected;
		}
		public Game getGame(int index) {
			return games.get(index);
		}
		
		public Game bestGeneratedGame() {
			return null;
		}
		public ArrayList<Double> fitnessScoresGenerated() {
			return null;
		}
		public ArrayList<Double> fitnessScoresNotGenerated() {
			return null;
		}
		
	}
	
	public static void writeToCsv(ArrayList<Double> fitnessValues, int epoch , boolean append) {
		// Our example data
		
		FileWriter csvWriter;
		try {
			csvWriter = new FileWriter("fitnessScores.csv",append);
			 
	
			for (Double rowData : fitnessValues) {
			    csvWriter.append(Double.toString(rowData));
			    csvWriter.append(","+Integer.toString(epoch));
			    csvWriter.append("\n");
			}
	
			csvWriter.flush();
			csvWriter.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeBestGame(ArrayList<Double> scores, ArrayList<Game> games, int epoch) {
		int largest = 0;
		for ( int i = 1; i < scores.size(); i++ )
		{
		      if ( scores.get(i) > scores.get(largest) ) largest = i;
		}
		//System.out.println("Best game of current epoch:");
		//System.out.println(games.get(largest).description().tokenForest().tokenTree());
	
		FileWriter Writer;
		try {
			Writer = new FileWriter("bestGame"+epoch+".lud");
			Writer.append(games.get(largest).description().tokenForest().tokenTree().toString());
			Writer.flush();
			Writer.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public static void main(final String [] args) {
		//final Game G1 = GameLoader.loadGameFromName("Checkers.lud");
		//final Game G2 = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");
		Generator gen = new Generator();
		
		// check for all tokens contained in both lists

		//this is only a temporary potentially good seeting 
		//gen.evaluateGame(G2, new Report(), 4, 50, 0.05, "Ludii AI", true);
		
		ArrayList<Game> games = gen.createInitialPopulation(50, false);
		ArrayList<Double> scoresInitial = gen.evaluateGames(games);
		writeToCsv(scoresInitial, 0, false);
		Genepool genepool = gen.new Genepool();
		/*
		ArrayList<Double> scoresInitial = new ArrayList();
		for(int i= 0;i<games.size();i++ ) {
			scoresInitial.add(1.0);
		}*/
		genepool.add(games, scoresInitial, false);
		
		System.out.println("Inital populaltion has size" + games.size());
		int epochs = 1;
		
		for (int i=0; i< epochs; i++) {
			System.out.println("Epoch: "+ i + "/"+ epochs);
			ArrayList<Game> GeneratedGames = gen.crossGamesRandom(genepool,50);
			ArrayList<Double> scores = gen.evaluateGames(GeneratedGames);
			genepool.add(GeneratedGames, scores, false);
			writeToCsv(scores,i, true);
			writeBestGame(scores,GeneratedGames,i);
		}
	}
}

