/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.norm;

import java.util.List;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * A score normalization algorithm.
 * Provides methods for normalizing a list of document scores. 
 * 
 * @author Ilya Markov
 */
public interface ScoreNormalization {
	
	/**
	 * Returns a list of normalized document scores
	 * for a given list of unnormalized scores.
	 * 
	 * @param unnormScoredDocs The list of document scores to be normalized.
	 * 
	 * @throws NullPointerException
	 * 		If <code>unnormScoredDocs</code> is <code>null</code>.
	 * 
	 * @return The list of normalized document scores.
	 */
	List<ScoredEntity<Object>> normalize(List<ScoredEntity<Object>> unnormScoredDocs);
}
