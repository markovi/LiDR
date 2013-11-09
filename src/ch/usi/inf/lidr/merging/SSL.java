/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.merging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.regression.SimpleRegression;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * The semi-supervised learning (SSL) results merging/score normalization algorithm.
 * SSL first learns the transformation function between source-specific
 * and centralized (normalized) document scores
 * based on a set of training data-points.
 * Then this function is applied to source-specific document scores,
 * thus normalizing them.
 * 
 * <p>
 * This implementation assumes that each search engine
 * uses its own scoring function,
 * possibly different from scoring functions of other search engines
 * (this corresponds to the "multiple search engine types" case in the paper, section 3.3).
 * </p>
 * 
 * @author Ilya Markov
 * 
 * @see "A semisupervised learning method to merge search engine results",
 * 		Luo Si and Jamie Callan.
 * 		<i>ACM Transactions on Information Systems</i>, pages 457-491, 2003.
 */
public final class SSL implements ResultsMerging {
	
	/**
	 * The mapping between sample documents and their centralized (normalized) scores for a given query.
	 * Used for training SSL.
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> the mapping must be set for each query using {@link #setSampleDocuments(List)}
	 * before normalizing document scores.
	 * </p>
	 * 
	 * @see #setSampleDocuments(List)
	 */
	private final Map<Object, Double> centrScores = new HashMap<Object, Double>();
	
	/**
	 * Sets the list of sampled documents.
	 * This list must contain documents,
	 * sampled from all sources of information/search engines.
	 * Scores of all documents must be calculated by one
	 * single scoring function for a given query. 
	 * 
	 * <p>
	 * In other words, the following steps must be performed
	 * in order to obtain the <code>sampleScoredDocs</code> list.
	 * <ol>
	 * <li>Sample a number of documents from each
	 * source of information/search engine.</li>
	 * <li>Create a single index out of all these documents.</li>
	 * <li>Run a given query on this index.</li>
	 * <li>Wrap obtained results into a list of {@link ScoredEntity} objects.</li>
	 * <li>Pass this list to the current method.</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> this method must be called for each query
	 * before performing normalization.
	 * </p>
	 * 
	 * @param sampleScoredDocs The list of documents,
	 * 		sampled from all sources of information/search engines,
	 * 		with scores calculated by one single scoring function.
	 * 
	 * @throws NullPointerException
	 * 		if <code>sampleScoredDocs</code> is <code>null</code>.
	 * 
	 * @see #centrScores
	 */
	public void setSampleDocuments(List<ScoredEntity<Object>> sampleScoredDocs) {
		if (sampleScoredDocs == null) {
			throw new NullPointerException("The list of sample scored documents is null.");
		}
		
		for (ScoredEntity<Object> scoredDocument : sampleScoredDocs) {
			centrScores.put(scoredDocument.getEntity(), scoredDocument.getScore());
		}
	}

	/**
	 * <b>IMPORTANT:</b> {@link #setSampleDocuments(List)} must be called before running normalization.
	 * 
	 * @see ch.usi.inf.lidr.norm.ScoreNormalization#normalize(java.util.List)
	 * @see #setSampleDocuments(List)
	 */
	@Override
	public List<ScoredEntity<Object>> normalize(List<ScoredEntity<Object>> unnormScoredDocs) {
		if (unnormScoredDocs == null) {
			throw new NullPointerException("The list of scored documents is null.");
		}
		
		SimpleRegression regression = getRegression(unnormScoredDocs);
		//TODO: backup with CORI
		if (regression.getN() < 3) {
			return new ArrayList<ScoredEntity<Object>>();
		}
		
		List<ScoredEntity<Object>> normScoredDocs = new ArrayList<ScoredEntity<Object>>(unnormScoredDocs.size());
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			ScoredEntity<Object> normScoredDoc = new ScoredEntity<Object>(unnormScoredDocs.get(i).getEntity(),
					regression.getSlope() * unnormScoredDocs.get(i).getScore() + regression.getIntercept());
			normScoredDocs.add(normScoredDoc);
		}
		
		return normScoredDocs;
	}
	
	/**
	 * Creates and returns a {@link SimpleRegression}
	 * for a given list of scored documents <code>scoredDocs</code>.
	 * This regression maps unnormalized scores in <code>scoredDocs</code>
	 * to normalized/centralized scores in <code>centrScores</code>.
	 * Documents that appear both in <code>scoredDocs</code>
	 * and <code>centrScores</code> are used as a training for the regression.
	 * According to the original paper, only first 10
	 * documents are considered for training.
	 * 
	 * @param scoredDocs The list of scored documents.
	 * 
	 * @return The {@link SimpleRegression} with filled-in training data.
	 */
	private SimpleRegression getRegression(List<ScoredEntity<Object>> scoredDocs) {
		SimpleRegression regression = new SimpleRegression();
		
		Set<Double> xData = new HashSet<Double>();
		for (ScoredEntity<Object> scoredDocument : scoredDocs) {
			Object docId = scoredDocument.getEntity();
			double specificScore = scoredDocument.getScore();
			
			if (centrScores.containsKey(docId) && !xData.contains(specificScore)) {
				regression.addData(specificScore, centrScores.get(docId));
				xData.add(specificScore);
				
				if (regression.getN() >= 10) {
					return regression;
				}
			}
		}
		
		return regression;
	}
}
