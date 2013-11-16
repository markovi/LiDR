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
 * Collection-integral Source Selection (CiSS) works as follows:
 * <ol>
 * <li>A user's query is submitted to CSI.</li>
 * <li>The retrieved documents are assigned to resources from which they originated.
 * Sample document ranks are discarded and are replaced with intra-collection ranks.
 * The last document in each resource is assumed to have the relevance score of 0
 * and the rank <code>result_length * |resource_size|/|sample_size|</code>.</li>
 * <li>Document scores are transformed exponentially and ranks are transformed logarithmically.</li>
 * <li>The score of each resource is calculated as an integral below the transformed score-rank curve.</li>
 * </ol>
 * 
 * @author Ilya Markov
 * 
 * @see "Integral based source selection for uncooperative distributed information retrieval environments",
 * 		Georgios Paltoglou, Michail Salampasisl and Maria Satratzemi.
 * 		In <i>Proceedings of LSDS-IR workshop</i>, pages 67-74, 2008.
 * @see "Modeling information sources as integrals for effective and efficient source selection",
 * 		Georgios Paltoglou, Michail Salampasisl and Maria Satratzemi.
 * 		<i>Information Processing & Management</i>, 47:1, pages 18-36, 2011.
 */
public class CiSS extends AbstractResourceSelection {

	/**
	 * @see ch.usi.inf.lidr.selection.AbstractResourceSelection#getResourceScores(List, java.util.List)
	 */
	@Override
	protected <T> Map<Resource, Double> getResourceScores(
			List<ScoredEntity<T>> documents, List<Resource> resources)
	{
		Map<Resource, Double> resourceScores = new HashMap<Resource, Double>();
		
		int rankCutoff = sampleRankCutoff > 0 ? sampleRankCutoff :
			getSampleRank(documents, resources, completeRankCutoff);
		
		List<ScoredEntity<T>> cutDocuments = documents.subList(0, Math.min(documents.size(), rankCutoff));
		List<Resource> cutResources = resources.subList(0, Math.min(resources.size(), rankCutoff));
		Map<Resource, List<ScoredEntity<T>>> document2resource = getDocument2Resource(cutDocuments, cutResources);
		
		for (Resource resource : document2resource.keySet()) {
			List<ScoredEntity<T>> resourceDocuments = document2resource.get(resource);
			resourceScores.put(resource, getResourceScore(resource, resourceDocuments));
		}
		
		return resourceScores;
	}
	
	/**
	 * Returns the score for a given <code>resource</code>
	 * based on its list of documents.
	 * 
	 * <p>
	 * Can be overridden by subclasses.
	 * </p>
	 * 
	 * @param resource The resource.
	 * @param documents Its list of scored documents.
	 * 
	 * @return The score for a given <code>resource</code>.
	 */
	protected <T> double getResourceScore(Resource resource, List<ScoredEntity<T>> documents) {
		if (documents.size() == 0) return 0;
		
		double resourceScore = 0;
		
		double leftScore = Math.exp(documents.get(0).getScore());
		double leftRank = Math.log(1);
		double rightScore = 0;
		double rightRank = 0;
		
		for (int i = 1; i < documents.size(); i++) {
			rightScore = Math.exp(documents.get(i).getScore());
			rightRank = Math.log(i + 1);
			
			resourceScore += (rightRank - leftRank) * (leftScore + rightScore) / 2;
			leftScore = rightScore;
			leftRank = rightRank;
		}
		
		rightScore = 0;
		rightRank = Math.log(resource.getSize() / (double) resource.getSampleSize() * documents.size());
		resourceScore += (rightRank - leftRank) * (leftScore + rightScore) / 2;
		
		return resourceScore;
	}

}
