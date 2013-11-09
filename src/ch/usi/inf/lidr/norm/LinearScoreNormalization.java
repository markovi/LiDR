/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.norm;

import java.util.ArrayList;
import java.util.List;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * The abstract implementation of linear score normalization methods.
 * 
 * @author Ilya Markov
 */
public abstract class LinearScoreNormalization implements ScoreNormalization {
	
	/**
	 * The rank at which an unnormalized list is truncated before normalization.
	 * If the list is shorter than {@link #rankCutoff},
	 * then zero-scored documents are added to it before normalization and removed after.
	 */
	private int rankCutoff = -1;
	
	/**
	 * Returns the rank cutoff.
	 * 
	 * @return The rank cutoff.
	 * 
	 * @see #rankCutoff
	 */
	public int getRankCutoff() {
		return rankCutoff;
	}

	/**
	 * Sets the rank cutoff.
	 * If not set or set to a negative value,
	 * the length of the list is not modified.
	 * 
	 * <p>
	 * The rank cutoff can be used, for example, if normalization
	 * needs to produce several normalized lists of the same length. 
	 * </p>
	 * 
	 * @param rankCutoff The rank to set. Should be greater than zero.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>rankCutoff</code> is less or equal to zero.
	 * 
	 * @see #rankCutoff
	 */
	public void setRankCutoff(int rankCutoff) {
		if (rankCutoff <= 0) {
			throw new IllegalArgumentException("The rank cutoff is less or equal to zero: " + rankCutoff);
		}
		this.rankCutoff = rankCutoff;
	}

	/**
	 * @see ch.usi.inf.lidr.norm.ScoreNormalization#normalize(java.util.List)
	 */
	@Override
	public List<ScoredEntity<Object>> normalize(List<ScoredEntity<Object>> unnormScoredDocs) {
		if (unnormScoredDocs == null) {
			throw new NullPointerException("The list of unnormalized document scores is null.");
		}
		
		List<ScoredEntity<Object>> normScoredDocs;
		
		if (rankCutoff >= 0) {
			List<ScoredEntity<Object>> truncScoredDocs = truncateScoredDocs(unnormScoredDocs, rankCutoff);
			truncScoredDocs = addZeroScoredDocs(truncScoredDocs, rankCutoff);
			normScoredDocs = doNormalization(truncScoredDocs);
			normScoredDocs = removeZeroScoredDocs(normScoredDocs, unnormScoredDocs.size());
		} else {
			normScoredDocs = doNormalization(unnormScoredDocs);
		}
		
		return normScoredDocs;
	}

	/**
	 * Truncates <code>scoredDocs</code> to the length of <code>rankCutoff</code>.
	 */
	private List<ScoredEntity<Object>> truncateScoredDocs(List<ScoredEntity<Object>> scoredDocs, int rankCutoff) {
		if (rankCutoff >= scoredDocs.size()) {
			return scoredDocs;
		}
		
		List<ScoredEntity<Object>> truncScoredDocs = new ArrayList<ScoredEntity<Object>>(scoredDocs.size());
		truncScoredDocs.addAll(scoredDocs);
		truncScoredDocs = truncScoredDocs.subList(0, rankCutoff);
		
		return truncScoredDocs;
	}
	
	/**
	 * If the length of <code>scoredDocs</code> is less than <code>rankCutoff</code>,
	 * then the list is extended by adding zero-scored documents.
	 */
	private List<ScoredEntity<Object>> addZeroScoredDocs(List<ScoredEntity<Object>> scoredDocs, int rankCutoff) {
		if (scoredDocs.size() >= rankCutoff) {
			return scoredDocs;
		}

		ScoredEntity<Object> zeroDoc = new ScoredEntity<Object>("zero-scored_document", 0);
		
		List<ScoredEntity<Object>> extendedScoredDocs = new  ArrayList<ScoredEntity<Object>>(rankCutoff);
		extendedScoredDocs.addAll(scoredDocs);
		for (int i = scoredDocs.size(); i < rankCutoff; i++) {
			extendedScoredDocs.add(zeroDoc);
		}
		
		return extendedScoredDocs;
	}
	
	/**
	 * Removes zero-scored documents from <code>scoredDocs</code> (if were added previously).
	 */
	private List<ScoredEntity<Object>> removeZeroScoredDocs(List<ScoredEntity<Object>> scoredDocs, int originalLength) {
		if (originalLength >= scoredDocs.size()) { 
			return scoredDocs;
		}
		return scoredDocs.subList(0, originalLength);
	}

	/**
	 * Performs score normalization.
	 * Must be overridden by subclasses.
	 * 
	 * @param unnormScoredDocs The list of document scores to be normalized.
	 * 
	 * @return The list of normalized document scores.
	 */
	protected abstract List<ScoredEntity<Object>> doNormalization(List<ScoredEntity<Object>> unnormScoredDocs);
}
