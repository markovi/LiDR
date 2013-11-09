/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

/**
 * ReDDE.top resource selection technique
 * for each document in the top-<i>L</i> increases
 * the score of a corresponding resource
 * by the document relevance score.
 * 
 * @author Ilya Markov
 * 
 * @see "Classification-based resource selection",
 * 		Jaime Arguello, Jamie Callan and Fernando Diaz.
 * 		In <i>Proceedings of CIKM</i>, pages 1277-1286, 2009.
 */
public final class ReDDETop extends ReDDE {

	/**
	 * @see ch.usi.inf.lidr.selection.ReDDE#getScoreAtRank(double, int)
	 */
	@Override
	protected double getScoreAtRank(double score, int rank) {
		return score;
	}

}
