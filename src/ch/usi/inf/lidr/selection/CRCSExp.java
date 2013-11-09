/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

/**
 * The exponential version of the CRCS resource selection
 * technique for each document in the top-<i>L</i> increases
 * the score of a corresponding resource by <code>exp^(-beta * doc_rank_sample)</code>.
 * 
 * @author Ilya Markov
 * 
 * @see "Central-Rank-Based Collection Selection in Uncooperative Distributed Information Retrieval",
 * 		Milad Shokouhi.
 * 		In <i>Proceedings of ECIR</i>, pages 160--172, 2007.
 */
public final class CRCSExp extends ReDDE {
	
	/**
	 * The coefficient of the exponential function.
	 */
	private double beta = 0.5;

	/**
	 * Returns the value of beta.
	 * 
	 * @return The value of beta.
	 * 
	 * @see #beta
	 */
	public double getBeta() {
		return beta;
	}

	/**
	 * Sets the value of beta.
	 * 
	 * @param beta The value of beta to set. Should be positive.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>beta</code> is less or equal to zero.
	 * 
	 * @see #beta
	 */
	public void setBeta(double beta) {
		if (beta <= 0) {
			throw new IllegalArgumentException("The beta is not positive: " + beta);
		}
		
		this.beta = beta;
	}

	/**
	 * @see ch.usi.inf.lidr.selection.ReDDE#getScoreAtRank(double, int)
	 */
	@Override
	protected double getScoreAtRank(double score, int rank) {
		return Math.exp(- beta * rank);
	}

}
