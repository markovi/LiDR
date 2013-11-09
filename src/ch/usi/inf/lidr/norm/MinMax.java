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
 * The MinMax score normalization algorithm
 * uses the following formula to normalize document scores:
 * <code>s_norm = (s - s_min) / (s_max - s_min)</code>,
 * where <code>s_min</code> and <code>s_max</code>
 * are the minimum and maximum scores in the original result list.
 * This way MinMax scales document scores to the range <code>[0, 1]</code>,
 * where the minimum normalized score is <code>0</code>,
 * while the maximum normalized score is <code>1</code>.
 * 
 * @author Ilya Markov
 * 
 * @see "Analyses of multiple evidence combination",
 * 		Joon Ho Lee.
 * 		In <i>Proceedings of SIGIR</i>, pages 267-276, 1997.
 */
public final class MinMax extends LinearScoreNormalization {

	/**
	 * @see ch.usi.inf.lidr.norm.LinearScoreNormalization#doNormalization(java.util.List)
	 */
	protected List<ScoredEntity<Object>> doNormalization(List<ScoredEntity<Object>> unnormScoredDocs) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			min = Math.min(min, unnormScoredDocs.get(i).getScore());
			max = Math.max(max, unnormScoredDocs.get(i).getScore());
		}
		
		double norm = max - min;
		if (norm == 0) {
			norm = 1;
		}

		List<ScoredEntity<Object>> normScoredDocs = new ArrayList<ScoredEntity<Object>>(unnormScoredDocs.size());
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			ScoredEntity<Object> normScoredDoc = new ScoredEntity<Object>(
					unnormScoredDocs.get(i).getEntity(),
					(unnormScoredDocs.get(i).getScore() - min) / norm);
			normScoredDocs.add(normScoredDoc);
		}
		
		return normScoredDocs;
	}
}
