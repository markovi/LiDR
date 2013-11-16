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
 * The Sum score normalization algorithm
 * first shifts the minimum score in a result list to zero: <code>s' = s - s_min</code>.
 * Then the shifted scores are divided by their sum: <code>s_norm = s' / sum(s')</code>.
 * This way the minimum normalized score is <code>0</code>,
 * while the sum of normalized scores is <code>1</code>.
 * 
 * @author Ilya Markov
 * 
 * @see "Relevance score normalization for metasearch",
 * 		M. Montague and J. A. Aslam.
 * 		In <i>Proceedings of CIKM<i>, pages 427-433, 2001.
 */
public final class Sum extends LinearScoreNormalization {

	/**
	 * @see ch.usi.inf.lidr.norm.LinearScoreNormalization#doNormalization(List<ScoredEntity<T>>)
	 */
	protected <T> List<ScoredEntity<T>> doNormalization(List<ScoredEntity<T>> unnormScoredDocs) {
		double min = Double.MAX_VALUE;
		double sum = 0;
		
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			min = Math.min(min, unnormScoredDocs.get(i).getScore());
			sum += unnormScoredDocs.get(i).getScore();
		}
		sum -= unnormScoredDocs.size() * min;
		
		if (sum == 0) {
			sum = 1;
		}
		
		List<ScoredEntity<T>> normScoredDocs = new ArrayList<ScoredEntity<T>>(unnormScoredDocs.size());
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			ScoredEntity<T> normScoredDoc = new ScoredEntity<T>(
					unnormScoredDocs.get(i).getEntity(),
					(unnormScoredDocs.get(i).getScore() - min) / sum);
			normScoredDocs.add(normScoredDoc);
		}
		
		return normScoredDocs;
	}
}
