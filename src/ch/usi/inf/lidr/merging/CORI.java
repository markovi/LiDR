/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.merging;

import java.util.ArrayList;
import java.util.List;

import ch.usi.inf.lidr.norm.MinMax;
import ch.usi.inf.lidr.norm.ScoreNormalization;
import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * Results merging/score normalization algorithm
 * that normalizes document scores using {@link MinMax}
 * and weighs them by the relevance of the result list.
 * 
 * @author Ilya Markov
 * 
 * @see "Searching distributed collections with inference networks",
 * 		James P. Callan, Zhihong Lu and W. Bruce Croft.
 * 		In <i>Proceedings of SIGIR</i>, pages 21-28, 1995.
 * @see "Advances in Information Retrieval, chapter 5. Distributed Information Retrieval",
 * 		Jamie Callan.
 * 		<i>Kluwer Academic Publishers</i>, pages 127-150, 2000.
 */
public class CORI implements ResultsMerging {
	
	/**
	 * The relevance of a result list (in the range <code>[0, 1]</code>).
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> for a list of document scores to be normalized,
	 * the relevance must be set first using {@link #setResultListRelevance(double)}
	 * and only then the normalization should be performed using {@link #normalize(List)}.
	 * If not set, the relevance <code>1</code> is used.
	 * </p>
	 * 
	 * @see #setResultListRelevance(double)
	 */
	private double resultListRelevance = 1d;
	
	/**
	 * CORI parameter that controls
	 * how much weight is given to the relevance of a result list.
	 */
	private double lambda = 0.4;

	/**
	 * The basic score normalization algorithm to be used by CORI.
	 * By default uses {@link MinMax}.
	 */
	private ScoreNormalization normalization = new MinMax();
	
	/**
	 * Sets the relevance of a list of document scores to be normalized.
	 * The relevance should be in the range <code>[0, 1]</code>.
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> this method should be called before {@link #normalize(List)}.
	 * If the relevance is not set, the value of <code>1</code> is used.
	 * </p>
	 * 
	 * @param resultListRelevance The relevance of a result list to be normalized.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>resultListRelevance</code> is not in the range <code>[0, 1]</code>.
	 * 
	 * @see #resultListRelevance
	 */
	public void setResultListRelevance(double resultListRelevance) {
		if (resultListRelevance < 0 || resultListRelevance > 1) {
			throw new IllegalArgumentException("The relevance of a result list to be normalized " +
					"is outside the range [0, 1]: " + resultListRelevance);
		}
		this.resultListRelevance = resultListRelevance;
	}

	/**
	 * Returns the value of the CORI parameter lambda.
	 * 
	 * @return The value of the CORI parameter lambda.
	 * 
	 * @see #lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * Sets the value of the CORI parameter lambda.
	 * 
	 * @param lambda The value of the parameter to set.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>lambda</code> is negative.
	 * 
	 * @see #lambda
	 */
	public void setLambda(double lambda) {
		if (lambda < 0) {
			throw new IllegalArgumentException("The CORI parameter lambda is negative: " + lambda);
		}
		this.lambda = lambda;
	}

	/**
	 * Returns the basic normalization algorithm.
	 * 
	 * @return The basic normalization algorithm.
	 * 
	 * @see #normalization
	 */
	public ScoreNormalization getNormalization() {
		return normalization;
	}

	/**
	 * Sets the basic normalization algorithm.
	 * 
	 * @param normalization The normalization algorithm to set.
	 * 
	 * @throws NullPointerException
	 * 		if <code>normalization</code> is <code>null</code>.
	 * 
	 * @see #normalization
	 */
	public void setNormalization(ScoreNormalization normalization) {
		if (normalization == null) {
			throw new NullPointerException("The basic normalization algorithm is null.");
		}
		this.normalization = normalization;
	}

	/**
	 * <b>IMPORTANT:</b> set the relevance of a result list to be normalized
	 * by {@link #setResultListRelevance(double)} before running this method.
	 * 
	 * @see ch.usi.inf.lidr.norm.ScoreNormalization#normalize(java.util.List)
	 */
	@Override
	public List<ScoredEntity<Object>> normalize(List<ScoredEntity<Object>> unnormScoredDocs) {
		if (unnormScoredDocs == null) {
			throw new NullPointerException("The list of scored documents is null.");
		}
		if (unnormScoredDocs.size() == 0) {
			return new ArrayList<ScoredEntity<Object>>();
		}
		
		List<ScoredEntity<Object>> normScoredDocs = normalization.normalize(unnormScoredDocs);
		List<ScoredEntity<Object>> weightedNormScoredDocs = new ArrayList<ScoredEntity<Object>>(unnormScoredDocs.size());
		
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			double score = getNormalizedScore(normScoredDocs.get(i).getScore(), resultListRelevance);
			ScoredEntity<Object> normScoredDoc = new ScoredEntity<Object>(unnormScoredDocs.get(i).getEntity(), score);
			weightedNormScoredDocs.add(normScoredDoc);
		}
		
		reset();
		return weightedNormScoredDocs;
	}
	
	/**
	 * Calculates the CORI normalized document score
	 * based on <code>docScore</code> and <code>resultListRelevance</code>.
	 * 
	 * <p>
	 * This method can be overridden by subclasses
	 * in order to change the calculation of the normalized
	 * document score.
	 * </p>
	 *  
	 * @param docScore The document score.
	 * @param resultListRelevance The relevance of a corresponding result list.
	 * 
	 * @return The CORI normalized document score.
	 */
	protected double getNormalizedScore(double docScore, double resultListRelevance) {
		return docScore * (1 + lambda * resultListRelevance) / (1 + lambda);
	}

	/**
	 * Resets {@link #resultListRelevance}.
	 */
	private void reset() {
		resultListRelevance = 1;
	}
}
