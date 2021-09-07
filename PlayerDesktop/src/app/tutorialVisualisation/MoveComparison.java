package app.tutorialVisualisation;

import java.util.ArrayList;
import java.util.List;

import game.util.directions.Direction;
import other.context.Context;
import other.move.Move;
import other.topology.Topology;

public class MoveComparison
{

	// Change these parameters to influence what is important when comparing moves.
	private final static boolean compateMoveType = false;				// The type/description of the move
	private final static boolean compareWhat = true;					// Piece being moved
	private final static boolean compareMover = true;					// The mover
	private final static boolean compareEnglishDescription = true;		// movesLudemes.toEnglish()
	private final static boolean compareDirection = false;				// The direction of the move
	private final static boolean compareActions = true;					// The actions in the move
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines if two moves can be merged due to them containing the same key information.
	 */
	public final static boolean movesCanBeMerged(final Topology topo, final MoveCompleteInformation m1, final MoveCompleteInformation m2)
	{
		if (compateMoveType)
			if (!m1.move().getDescription().equals(m2.move().getDescription()))
				return false;
		
		if (compareWhat)
			if (!m1.pieceName().equals(m2.pieceName()))
				return false;
		
		if (compareMover)
			if (m1.move().mover() != m2.move().mover())
				return false;

		if (compareEnglishDescription)
			if (!m1.englishDescription().equals(m2.englishDescription()))
				return false;
		
		// Check if the moves directions are the same.
		if (compareDirection)
		{
			Direction m1Direction = m1.move().direction(topo);
			Direction m2Direction = m2.move().direction(topo);
			if 
			(
				m1Direction == null && m2Direction != null
				||
				m1Direction != null && m2Direction == null
				||
				!m1Direction.equals(m2Direction)
			)
				return false;
		}
		
		if (compareActions)
		{
			if (m1.move().actions().size() != m2.move().actions().size())
				return false;
			
			for (int i = 0; i < m1.move().actions().size(); i++)
			{
				final String m1ActionDescription = m1.move().actions().get(i).getDescription();
				final String m2ActionDescription = m2.move().actions().get(i).getDescription();
				if (!m1ActionDescription.equals(m2ActionDescription))
					return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns a list off all similar legal moves at the point in the context where the trueMove was applied.
	 * Similar moves are those that can be merged, and are from the same location.
	 */
	public final static List<Move> similarMoves(final Context context, final Move trueMove)
	{
		final int trueMoveWhat = ValueUtils.getWhatOfMove(context, trueMove);
		final MoveCompleteInformation trueMoveCompleteInfo = new MoveCompleteInformation(context.game(), null, null, trueMove, -1, ValueUtils.getComponentNameFromIndex(context, trueMoveWhat), null);
		
		final List<Move> similarMoves = new ArrayList<>();
		for (final Move move : context.moves(context).moves())
		{
			final Move moveWithConsequences = new Move(move.getMoveWithConsequences(context));
			moveWithConsequences.setMovesLudeme(move.movesLudeme());
			
			final int moveWhat = ValueUtils.getWhatOfMove(context, moveWithConsequences);
			final MoveCompleteInformation moveCompleteInfo = new MoveCompleteInformation(context.game(), null, null, moveWithConsequences, -1, ValueUtils.getComponentNameFromIndex(context, moveWhat), null);
			
			if (movesCanBeMerged(context.topology(), trueMoveCompleteInfo, moveCompleteInfo) && moveWithConsequences.getFromLocation().equals(trueMove.getFromLocation()))
				similarMoves.add(new Move(moveWithConsequences));
		}
		
		if (similarMoves.isEmpty())
			System.out.println("ERROR! similarMoves was empty");
		
		return similarMoves;
	}
	
	//-------------------------------------------------------------------------
	
}
