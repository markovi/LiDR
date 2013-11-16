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
 * The Z-Score normalization algorithm
 * uses the following formula to normalize document scores:
 * <code>s_norm = (s - mu) / sigma</code>,
 * where <code>mu</code> and <code>sigma</code> are the mean and standard deviation
 * of document scores in the original result list.
 * This way the mean normalized score is <code>1</code>,
 * while other normalized scores are distributed around it with the stadard deviation <code>1</code>.
 * 
 * @author Ilya Markov
 * 
 * @see "Relevance score normalization for metasearch",
 * 		M. Montague and J. A. Aslam.
 * 		In <i>Proceedings of CIKM<i>, pages 427-433, 2001.
 */
public final class ZScore extends LinearScoreNormalization {

	/**
	 * @see ch.usi.inf.lidr.norm.LinearScoreNormalization#doNormalization(List<ScoredEntity<T>>)
	 */
	protected <T> List<ScoredEntity<T>> doNormalization(List<ScoredEntity<T>> unnormScoredDocs) {
		double mu = 0;
		double temp = 0;
		
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			double x = unnormScoredDocs.get(i).getScore();
			
			double delta = x - mu;
			mu += delta / (i + 1);
			
			if (i > 0) {
				temp += delta * (x - mu);
			}
		}
		
		double sigma = Math.sqrt(temp / (unnormScoredDocs.size() - 1));
		if (sigma == 0) {
			sigma = 1;
		}

		List<ScoredEntity<T>> normScoredDocs = new ArrayList<ScoredEntity<T>>(unnormScoredDocs.size());
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			ScoredEntity<T> normScoredDoc = new ScoredEntity<T>(
					unnormScoredDocs.get(i).getEntity(),
					(unnormScoredDocs.get(i).getScore() - mu) / sigma);
			normScoredDocs.add(normScoredDoc);
		}
		
		return normScoredDocs;
	}
}
