/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.norm;

import java.util.List;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * Performs no normalization and returns original scores as they are.
 * 
 * @author Ilya Markov
 */
public final class Identity extends LinearScoreNormalization {

	/**
	 * @see ch.usi.inf.lidr.norm.LinearScoreNormalization#doNormalization(java.util.List)
	 */
	@Override
	protected List<ScoredEntity<Object>> doNormalization(List<ScoredEntity<Object>> scoredDocs) {
		return scoredDocs;
	}

}
