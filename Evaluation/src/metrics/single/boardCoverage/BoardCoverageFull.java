package metrics.single.boardCoverage;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Percentage of all board sites which a piece was placed on at some point.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverageFull extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverageFull()
	{
		super
		(
			"Board Coverage Full", 
			"Percentage of all board sites which a piece was placed on at some point.", 
			0.0, 
			1.0,
			Concept.BoardCoverageFull
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double numSitesCovered = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record all sites covered in this trial.
			final Set<TopologyElement> sitesCovered = new HashSet<TopologyElement>();
			
			sitesCovered.addAll(Utils.boardAllSitesCovered(context));
			for (final Move m : trial.generateRealMovesList())
			{
				context.game().apply(context, m);
				sitesCovered.addAll(Utils.boardAllSitesCovered(context));
			}
			
			numSitesCovered += ((double) sitesCovered.size()) / game.board().topology().getAllGraphElements().size();
		}

		return numSitesCovered / trials.length;
	}

}
