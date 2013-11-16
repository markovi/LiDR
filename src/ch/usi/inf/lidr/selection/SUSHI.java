/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * For each resource SUSHI learns the transformation
 * function from a document complete rank
 * to a document centralized score: <code>score_c = f(rank_complete)</code>.
 * The complete rank is estimated as
 * <code>rank_complete = resource_size / size_of_sample * (rank_sample - 0.5)</code>,
 * where <code>rank_sample</code> is the rank of a document in CSI.
 * 
 * @author Ilya Markov
 * 
 * @see "SUSHI: scoring scaled samples for server selection",
 * 		Paul Thomas and Milad Shokouhi.
 * 		In <i>Proceedings of SIGIR</i>, pages 419--426, 2009.
 */
public final class SUSHI extends AbstractResourceSelection {
	
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
		 * The slope of the regression.
		 */
		private double slope = Double.NaN;
		/**
		 * The intercept of the regression.
		 */
		private double intercept = Double.NaN;
		
		/**
		 * Adds the observation <code>(f(x), y)</code>
		 * to the regression data set. 
		 * 
		 * @param x The independent variable value.
		 * @param y The dependent variable value.
		 */
		public void addData(double x, double y) {
			regression.addData(f(x), y);
			reset();
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
		 * Returns 0 if the number of added observations is less than 2.
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
			
			if (Double.isNaN(slope) || Double.isNaN(intercept)) {
				slope = regression.getSlope();
				intercept = regression.getIntercept();
			}
			return slope * f(x) + intercept;
		}
		
		/**
		 * Returns the coefficient of determination
		 * usually denoted as r^2.
		 * 
		 * <p>
		 * Returns 0 if the number of added observations is less than 2.
		 * </p>
		 * 
		 * @return Pearson's r.
		 */
		public double getRSquare() {
			if (getN() < 2) {
				return 0;
			}
			
			return regression.getRSquare();
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
		
		/**
		 * Resets the slope and intercept of the regression.
		 */
		private void reset() {
			slope = Double.NaN;
			intercept = Double.NaN;
		}
	}
	
	
	/**
	 * The minimum number of documents that each resource needs to have
	 * for fitting a regression.
	 */
	private int minNumDocs = 5;
	
	/**
	 * A <i>complete</i> rank above which documents are considered to be relevant.
	 */
	private int rankThreshold = 1000;

	/**
	 * Returns the rank threshold.
	 * 
	 * @return The rank threshold.
	 * 
	 * @see #rankThreshold
	 */
	public int getRankThreshold() {
		return rankThreshold;
	}

	/**
	 * Sets the rank threshold.
	 * The rank threshold should be positive.
	 * 
	 * @param rankThreshold The rank threshold.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>rankThreshold</code> is less than or equal to zero.
	 * 
	 * @see #rankThreshold
	 */
	public void setRankThreshold(int rankThreshold) {
		if (rankThreshold <= 0) {
			throw new IllegalArgumentException("The rank threshold is less or equal to zero: " + rankThreshold);
		}
		this.rankThreshold = rankThreshold;
	}

	/**
	 * Returns the minimum number of documents required for fitting a regression.
	 * 
	 * @return The minimum number of documents required for fitting a regression.
	 * 
	 * @see #minNumDocs
	 */
	public int getMinNumDocs() {
		return minNumDocs;
	}

	/**
	 * Sets the minimum number of documents required for fitting a regression.
	 * It must not be less than 2.
	 * 
	 * @param minNumDocs The minimum number of documents required for fitting a regression.
	 * 
	 * @throws IllegalArgumentException
	 * 		if <code>minNumDocs</code> is less than 2.
	 * 
	 * @see #minNumDocs
	 */
	public void setMinNumDocs(int minNumDocs) {
		if (minNumDocs < 2) {
			throw new IllegalArgumentException("The minimum number of documents is less than 2: " + minNumDocs);
		}
		this.minNumDocs = minNumDocs;
	}

	/**
	 * @see ch.usi.inf.lidr.selection.AbstractResourceSelection#getResourceScores(List, java.util.List)
	 */
	@Override
	protected <T> Map<Resource, Double> getResourceScores(
			List<ScoredEntity<T>> documents, List<Resource> resources)
	{
		Map<Resource, Regression> resource2regression = getResource2Regression(documents, resources);

		int currentRankCutoff = sampleRankCutoff > 0 ? sampleRankCutoff :
			getSampleRank(documents, resources, completeRankCutoff);
		
		List<ScoredEntity<T>> completeDocuments = new ArrayList<ScoredEntity<T>>();
		List<Resource> completeResources = new ArrayList<Resource>();
		for (int i = 0; i < documents.size() && i < currentRankCutoff; i++) {
			if (!resource2regression.containsKey(resources.get(i))) {
				completeDocuments.add(documents.get(i));
				completeResources.add(resources.get(i));
			}
		}
		getCompleteDocumentRanking(resource2regression, completeDocuments, completeResources);
		
		Map<Resource, Double> resourceScores = calculateResourceScores(completeDocuments, completeResources);
		return resourceScores;
	}
	
	
	/**
	 * For each distinct resource in <code>resources</code>
	 * returns the best-fit regression between scores and ranks
	 * of documents in that resource.
	 */
	private <T> Map<Resource, Regression> getResource2Regression(
			List<ScoredEntity<T>> documents, List<Resource> resources)
	{
		Map<Resource, Regression[]> resource2regressions = new HashMap<Resource, Regression[]>();
		
		int currentRankCutoff = sampleRankCutoff > 0 ? sampleRankCutoff :
			getSampleRank(documents, resources, completeRankCutoff);
		
		int centrRank = 0;
		for (int i = 0; i < documents.size() && i < currentRankCutoff; i++) {
			double docScore = documents.get(i).getScore();
			Resource resource = resources.get(i);
			Regression[] regressions = resource2regressions.get(resource) != null ?
					resource2regressions.get(resource) : getRegressions();
			
			int sizeRatio = resources.get(i).getSize() / resources.get(i).getSampleSize();
			int docRank = (int) (centrRank + 0.5 * sizeRatio);
			centrRank += sizeRatio;
			
			for (int j = 0; j < regressions.length; j++) {
				regressions[j].addData(docRank, docScore);
			}
			
			resource2regressions.put(resource, regressions);
		}
		
		Map<Resource, Regression> result = new HashMap<Resource, Regression>();
		for (Map.Entry<Resource, Regression[]> resource2regression : resource2regressions.entrySet()) {
			Resource resource = resource2regression.getKey();
			Regression regression = getBestFitRegression(resource2regression.getValue());
			if (regression.getN() >= minNumDocs) {
				result.put(resource, regression);
			}
		}
		return result;
	}
	
	/**
	 * Returns three different regression models, namely,
	 * linear, log, and exponential.
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
					return Math.exp(x);
				}
			}
		};
	}
	
	/**
	 * Finds a regression with the highest r^2 coefficient
	 * among given <code>regressions</code>.
	 */
	private Regression getBestFitRegression(Regression[] regressions) {
		Regression result = regressions[0];
		for (int i = 1; i < regressions.length; i++) {
			if (result.getRSquare() < regressions[i].getRSquare()) {
				result = regressions[i];
			}
		}
		
		return result;
	}
	
	
	/**
	 * Based on the given regressions
	 * estimates the scores of the top-<i>L</i> documents,
	 * where <code>L = rankThreshold</code>.
	 */
	private <T> void getCompleteDocumentRanking(Map<Resource, Regression> resource2regression,
			List<ScoredEntity<T>> documents, List<Resource> resources)
	{
		if (resource2regression.size() == 0) {
			return;
		}
		
		for (Map.Entry<Resource, Regression> res2regr : resource2regression.entrySet()) {
			Resource resource = res2regr.getKey();
			Regression regression = res2regr.getValue();
			
			for (int i = 1; i <= rankThreshold; i++) {
				double score = regression.predict(i);
				documents.add(new ScoredEntity(resource + "_" + i, score));
				resources.add(resource);
			}
			
		}
		
		sort(documents, resources, rankThreshold);
		documents = documents.subList(0, Math.min(documents.size(), rankThreshold));
		resources = resources.subList(0, Math.min(resources.size(), rankThreshold));
	}

	
	/**
	 * Calculates resource scores based on the list of scored documents.
	 */
	private <T> Map<Resource, Double> calculateResourceScores(
			List<ScoredEntity<T>> documents, List<Resource> resources)
	{
		Map<Resource, Double> resourceScores = new HashMap<Resource, Double>();
		for (Resource resource : resources) {
			resourceScores.put(resource, 0d);
		}
		
		for (int i = 0; i < documents.size() && i < rankThreshold; i++) {
			Resource resource = resources.get(i);
			
			double score = resourceScores.get(resource);
			score += documents.get(i).getScore();
			
			resourceScores.put(resource, score);
		}
		
		return resourceScores;
	}
}
