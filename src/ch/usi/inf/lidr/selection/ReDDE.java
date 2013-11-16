/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * ReDDE resource selection, the first SD technique.
 * Methods of this type work as follows.
 * <ol>
 * <li>Sample a number of documents from each resource.</li>
 * <li>Index all sampled documents in a centralized sample index (CSI).</li>
 * <li>For a given user's query rank/score documents in CSI.</li>
 * <li>Consider the top <i>L</i> documents from this ranking.</li>
 * <li>Calculate a score for each resource based on its documents
 * that appear in the top-<i>L</i>.</li>
 * </ol>
 * 
 * <p>
 * SD resource selection methods differ mainly in the way
 * they calculate resource scores based on the documents in the top-<i>L</i>.
 * For each resource ReDDE counts the number of
 * its documents that appear in the top-<i>L</i>.
 * Than this number is scaled by <code>resource_size / size_of_sample</code>.
 * Other methods may change this behavior
 * by overriding {@link #getScoreAtRank(double, int)}.
 * </p>
 * 
 * @author Ilya Markov
 * 
 * @see "Relevant document distribution estimation method for resource selection",
 * 		Luo Si and Jamie Callan.
 * 		In <i>Proceedings of SIGIR</i>, pages 298-305, 2003.
 */
public class ReDDE extends AbstractResourceSelection {
	
	/**
	 * The same as {@link #sampleRankCutoff}, but set for each query.
	 * If {@link #sampleRankCutoff} is not set,
	 * then {@link #currentRankCutoff} is calculated based on {@link #completeRankCutoff}.
	 */
	protected int currentRankCutoff = -1;
	
	/**
	 * @see ch.usi.inf.lidr.selection.AbstractResourceSelection#getResourceScores(List, java.util.List)
	 */
	@Override
	protected <T> Map<Resource, Double> getResourceScores(
			List<ScoredEntity<T>> documents, List<Resource> resources)
	{
		Map<Resource, Double> resourceScores = new HashMap<Resource, Double>();

		currentRankCutoff = sampleRankCutoff > 0 ? sampleRankCutoff :
			getSampleRank(documents, resources, completeRankCutoff);
		
		for (int i = 0; i < documents.size() && i < currentRankCutoff; i++) {
			Resource resource = resources.get(i);
			
			double score = resourceScores.containsKey(resource) ? resourceScores.get(resource) : 0;
			score += getScoreAtRank(documents.get(i).getScore(), i);
			
			resourceScores.put(resource, score);
		}
		
		for (Resource resource : resourceScores.keySet()) {
			double score = resourceScores.get(resource) * resource.getSize() / resource.getSampleSize();
			resourceScores.put(resource, score);
		}
		
		return resourceScores;
	}
	
	/**
	 * Returns a score that a resource receives
	 * if its document appears at a given rank.
	 * Can be overridden by subclasses.
	 * 
	 * <p>
	 * Both <code>score</code> and <code>rank</code>
	 * are calculated based on a centralized sample index (CSI).
	 * </p>
	 * 
	 * @param score The document score.
	 * @param rank The document rank.
	 * 
	 * @return The score that a resource receives for a document
	 * 		at a given rank.
	 */
	protected double getScoreAtRank(double score, int rank) {
		return 1;
	}

}
