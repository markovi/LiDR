/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

/**
 * The linear version of the CRCS resource selection
 * technique for each document in the top-<i>L</i> increases
 * the score of a corresponding resource by <code>top_L - doc_rank_sample</code>.
 * 
 * @author Ilya Markov
 * 
 * @see "Central-Rank-Based Collection Selection in Uncooperative Distributed Information Retrieval",
 * 		Milad Shokouhi.
 * 		In <i>Proceedings of ECIR</i>, pages 160--172, 2007.
 */
public final class CRCSLinear extends ReDDE {

	/**
	 * @see ch.usi.inf.lidr.selection.ReDDE#getScoreAtRank(double, int)
	 */
	@Override
	protected double getScoreAtRank(double score, int rank) {
		return rank < currentRankCutoff ? currentRankCutoff - rank : 0;
	}
}
