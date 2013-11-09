/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.merging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;

import ch.usi.inf.lidr.utils.ScoredEntity;


/**
 * SAFE results merging/score normalization algorithm.
 * SAFE learns the transformation function between centralized document ranks
 * and centralized document scores.
 * Then this function is applied to source-specific ranks (<code>1</code>, <code>2</code>, ...)
 * to obtain corresponding normalized scores.
 * 
 * @author Ilya Markov
 * 
 * @see "Robust result merging using sample-based score estimates",
 * 		Milad Shokouhi and Justin Zobel.
 * 		<i>ACM Transactions on Information Systems</i>, 27:3, pages 1-29, 2009.
 */
public final class SAFE implements ResultsMerging {
	
	/**
	 * An abstract class that performs regression
	 * of the type <code>y = a * f(x) + b</code>, where the function
	 * <code>f</code> should be overridden by subclasses.
	 *  
	 * @author Ilya Markov
	 */
	private abstract static class Regression {
		/**
		 * The regression.
		 */
		private final SimpleRegression regression = new SimpleRegression();
		
		/**
		 * Adds the observation <code>(f(x), y)</code>
		 * to the regression data set. 
		 * 
		 * @param x The independent variable value.
		 * @param y The dependent variable value.
		 */
		public void addData(double x, double y) {
			regression.addData(f(x), y);
		}
		
		/**
		 * Returns the "predicted" y value associated
		 * with the supplied x value, based on the data that
		 * has been added to the model so far.
		 * In particular, <code>y = a * f(x) + b</code>,
		 * where <code>a</code> and <code>b</code>
		 * are estimated by the regression. 
		 * 
		 * <p>
		 * Returns 0 if number of added observations is less than 2.
		 * </p>
		 * 
		 * @param x The input <code>x</code> value.
		 * 
		 * @return The predicted <code>y</code> value.
		 */
		public double predict(double x) {
			if (getN() < 2) {
				return 0;
			}
			
			return regression.getSlope() * f(x) + regression.getIntercept();
		}
		
		/**
		 * Returns Pearson's product moment correlation coefficient,
		 * usually denoted as r. 
		 * 
		 * <p>
		 * Returns 0 if number of added observations is less than 2.
		 * </p>
		 * 
		 * @return Pearson's r.
		 */
		public double getR() {
			if (getN() < 2) {
				return 0;
			}
			
			return regression.getR();
		}
		
		/**
		 * Returns the number of observations that have been added to the model. 
		 * 
		 * @return The number of observations that have been added.
		 */
		public long getN() { 
			return regression.getN();
		}
		
		/**
		 * Returns <code>f(x)</code>. Must be implemented by subclasses.
		 * 
		 * @param x The <code>x</code> value.
		 * 
		 * @return The <code>f(x)</code> value.
		 */
		protected abstract double f(double x);
	}
	

	/**
	 * The rank ratio.
	 * This ratio means that
	 * if a document has a rank <code>r_s</code> in a sample,
	 * then it should have the rank <code>r_c = (r_s - 0.5) * rankRatio</code>
	 * in the original collection.
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> the rank ratio should be set using {@link #setRankRatio(double)}
	 * every time before running normalization.
	 * If it is not set, the ratio of 1 is used.
	 * </p>
	 * 
	 * @see #setRankRatio
	 */
	private double rankRatio = 1;
	
	/**
	 * The ranked list of sample documents.
	 * This list is used as an additional evidence/training data for performing SAFE score normalization.
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> the list of sampled documents must be set for each query
	 * using {@link #setSampledDocs(List)} before running normalization.
	 * </p>
	 * 
	 * @see #setSampledDocs(List)
	 */
	private List<ScoredEntity<Object>> sampledDocs = new ArrayList<ScoredEntity<Object>>();
	
	/**
	 * Sets the rank ratio.
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> this method should be called
	 * every time before running normalization.
	 * By default, the ratio of 1 is used.
	 * </p>
	 * 
	 * @param rankRatio The rank ratio. Should be positive.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>rankRatio</code> is not positive.
	 * 
	 * @see #rankRatio
	 */
	public void setRankRatio(double rankRatio) {
		if (rankRatio <= 0) {
			throw new IllegalArgumentException("The rank ratio is not positive: " + rankRatio);
		}
		
		this.rankRatio = rankRatio;
	}
	
	/**
	 * Sets the ranked list of sample documents.
	 * Sample documents' scores must be calculated by one single scoring function for a given query.
	 * Moreover, the scores must be calculated within a centralized
	 * index of documents, sampled from all sources of information/search engines.
	 * 
	 * <p>
	 * In other words, the following steps must be performed
	 * in order to obtain <code>sampledDocs</code>.
	 * <ol>
	 * <li>Sample a number of documents from each
	 * source of information/search engine.</li>
	 * <li>Create a single index out of all these documents.</li>
	 * <li>Run a given query on this index.</li>
	 * <li>Wrap obtained results into a ranked list of {@link ScoredEntity} objects.</li>
	 * <li>Extract documents belonging to a particular source and
	 * pass them to this method.</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> this method must be invoked for each query
	 * before running normalization.
	 * </p>
	 * 
	 * @param sampledDocs The ranked list of sample documents.
	 * 
	 * @throws NullPointerException
	 * 		if <code>sampledDocs</code> is <code>null</code>.
	 * 
	 * @see #sampledDocs
	 */
	public void setSampledDocs(List<ScoredEntity<Object>> sampledDocs) {
		if (sampledDocs == null) {
			throw new NullPointerException("The list of sample documents is null.");
		}
		
		this.sampledDocs = sampledDocs;
	}

	/**
	 * <b>IMPORTANT:</b> {@link #setSampledDocs(List)} must
	 * and {@link #setRankRatio(double)} should be invoked before performing normalization.
	 * 
	 * @see ch.usi.inf.lidr.norm.ScoreNormalization#normalize(java.util.List)
	 * @see #setSampledDocs(List)
	 * @see #setRankRatio(double)
	 */
	@Override
	public List<ScoredEntity<Object>> normalize(List<ScoredEntity<Object>> unnormScoredDocs) {
		if (unnormScoredDocs == null) {
			throw new NullPointerException("The list of scored documents is null.");
		}
		
		Map<Integer, Double> rank2score = getRank2ScoreMapping(sampledDocs, unnormScoredDocs);
		Regression[] regressions = getRegressions(rank2score);
		Regression hybrid = getBestFitRegression(regressions);
		
		if (hybrid.getN() < 3) {
			return new ArrayList<ScoredEntity<Object>>();	// ???
		}
		
		List<ScoredEntity<Object>> normScoredDocs = new ArrayList<ScoredEntity<Object>>(unnormScoredDocs.size());
		for (int i = 0; i < unnormScoredDocs.size(); i++) {
			normScoredDocs.add(new ScoredEntity<Object>(unnormScoredDocs.get(i).getEntity(), hybrid.predict(i + 1)));
		}
		
		reset();
		return normScoredDocs;
	}
	
	/**
	 * For sample documents in <code>sampledDocs</code> 
	 * calculates the correspondence between their estimated centralized ranks
	 * (based on {@link #rankRatio}) and centralized scores.
	 * If a document from <code>sampledDocs</code> appears
	 * also in <code>scoredDocs</code> then its true rank is used.
	 */
	private Map<Integer, Double> getRank2ScoreMapping(List<ScoredEntity<Object>> sampledDocs, List<ScoredEntity<Object>> scoredDocs) {
		Map<Integer, Double> rank2score = new HashMap<Integer, Double>();
		
		Map<Object, Integer> scoredDocRanks = getDoc2RankMap(scoredDocs);
		int lastSeenIndex = getOverlapDocs(sampledDocs, scoredDocRanks, rank2score);
		getNonoverlapDocs(sampledDocs, lastSeenIndex, scoredDocs.size(), rank2score);
		
		return rank2score;
	}
	
	/**
	 * For each sample document in <code>sampledDocs</code>
	 * searches for a corresponding document in the source-specific
	 * list <code>scoredDocRanks</code>.
	 * If the correspondence is found, the true document rank
	 * is mapped to a centralized score.
	 * 
	 * <p>
	 * Note the following situation. Let <code>sampledDocs[i]</code>
	 * and <code>sampledDocs[j]</code> have corresponding documents
	 * in <code>scoredDocRanks</code>. Then all sample documents
	 * between <code>i</code> and <code>j</code> are skipped
	 * and are not used as training data!
	 * This is because it is impossible to estimate their source-specific ranks.
	 * </p>
	 * 
	 * <p>
	 * Returns the index of the last sample document
	 * that has a correspondence in a source-specific list <code>scoredDocRanks</code>.
	 * </p>
	 * 
	 * @return The index of the last overlapping sample document.
	 */
	private int getOverlapDocs(List<ScoredEntity<Object>> sampledDocs, Map<Object, Integer> scoredDocRanks,
			Map<Integer, Double> rank2score)
	{
		int lastSeenIndex = -1;
		
		for (int i = 0; i < sampledDocs.size(); i++) {
			Object document = sampledDocs.get(i).getEntity();
			
			if (scoredDocRanks.containsKey(document)) {
				int rank = scoredDocRanks.get(document);
				double score = sampledDocs.get(i).getScore();
				lastSeenIndex = i;
				
				rank2score.put(rank, score);
			}
		}
		
		return lastSeenIndex;
	}
	
	/**
	 * For each sample document starting from <code>lastSeenIndex</code>
	 * estimates its source-specific rank according to the following formula:
	 * <code>r_c = offset + (r_s - 0.5) * rankRatio</code>.
	 */
	private void getNonoverlapDocs(List<ScoredEntity<Object>> sampledDocs, int lastSeenIndex, int offset,
			Map<Integer, Double> rank2score)
	{
		for (int i = lastSeenIndex + 1; i < sampledDocs.size(); i++) {
			int rank = (int) (offset + (i - lastSeenIndex - 0.5) * rankRatio);
			double score = sampledDocs.get(i).getScore();
			
			rank2score.put(rank, score);
		}
	}
	
	/**
	 * Transforms <code>scoredDocs</code> into a mapping
	 * between documents and their ranks.
	 */
	private Map<Object, Integer> getDoc2RankMap(List<ScoredEntity<Object>> scoredDocs) {
		Map<Object, Integer> doc2rankMap = new HashMap<Object, Integer>();
		for (int i = 0; i < scoredDocs.size(); i++) {
			doc2rankMap.put(scoredDocs.get(i).getEntity(), i + 1);
		}
		return doc2rankMap;
	}
	
	/**
	 * Creates a number of regressions and fills them
	 * with data from <code>rank2score</code>.
	 */
	private Regression[] getRegressions(Map<Integer, Double> rank2score) {
		Regression[] regressions = getRegressions();
		
		for (Regression regression : regressions) {
			for (Map.Entry<Integer, Double> entry : rank2score.entrySet()) {
				regression.addData(entry.getKey(), entry.getValue());
			}
		}
		
		return regressions;
	}
	
	/**
	 * Returns four different regression models, namely,
	 * linear, log, square root and inverse linear.
	 */
	private Regression[] getRegressions() {
		return new Regression[] {
			new Regression() {
				protected double f(double x) {
					return x;
				}
			},
			new Regression() {
				protected double f(double x) {
					if (x <= 0) {
						return Double.NEGATIVE_INFINITY;
					}
					return Math.log(x);
				}
			},
			new Regression() {
				protected double f(double x) {
					if (x < 0) {
						return 0;
					}
					return Math.sqrt(x);
				}
			},
			new Regression() {
				protected double f(double x) {
					if (x == 0) {
						return 0;
					}
					return 1 / x;
				}
			}
		};
	}
	
	/**
	 * Finds a regression with the highest Pearson's r coefficient
	 * among given <code>regressions</code>.
	 */
	private Regression getBestFitRegression(Regression[] regressions) {
		assert regressions != null : "An array pf regression should not be null";
		assert regressions.length > 0 : "An array of regressions must contain at least one element";
		
		Regression result = regressions[0];
		for (int i = 1; i < regressions.length; i++) {
			if (result.getR() < regressions[i].getR()) {
				result = regressions[i];
			}
		}
		
		return result;
	}

	/**
	 * Resets {@link #rankRatio} and {@link #sampledDocs}.
	 * 
	 * @see #setRankRatio(double)
	 * @see #setSampledDocs(List)
	 */
	private void reset() {
		rankRatio = 1;
		sampledDocs = new ArrayList<ScoredEntity<Object>>();
	}
	
}