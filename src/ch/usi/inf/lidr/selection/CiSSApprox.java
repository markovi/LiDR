/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

import java.util.List;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * CiSS resource selection,
 * where only the score of the top document from each resource is taken into account:
 * <code>1/2 * exp(doc_score_top) * log(result_length * |resource_size|/|sample_size|)</code>
 *  
 * @author Ilya Markov
 * 
 * @see CiSS
 */
public class CiSSApprox extends CiSS {

	/**
	 * @see ch.usi.inf.lidr.selection.CiSS#getResourceScore(ch.usi.inf.lidr.selection.Resource, java.util.List)
	 */
	@Override
	protected double getResourceScore(Resource resource, List<ScoredEntity<Object>> documents) {
		if (documents.size() == 0) return 0;
		
		double topScore = documents.get(0).getScore();
		double maxRank = resource.getSize() / (double) resource.getSampleSize() * documents.size();
		
		return getResourceScore(topScore, maxRank);
	}
	
	/**
	 * Returns the score of a resource based on the score of its top document
	 * and on the estimated maximum rank of its documents.
	 * 
	 * <p>
	 * Can be overridden by subclasses.
	 * </p>
	 * 
	 * @param topScore The score of the top document.
	 * @param maxRank The estimated maximum rank.
	 * 
	 * @return The score of a resource.
	 */
	protected double getResourceScore(double topScore, double maxRank) {
		return Math.exp(topScore) * Math.log(maxRank) / 2;
	}

}
