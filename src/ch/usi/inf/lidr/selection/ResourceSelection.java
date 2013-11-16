/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

import java.util.List;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * An interface for small-document (SD) approaches to resource selection.
 * These techniques select and rank resources based on the ranking of documents
 * in a centralized sample index (CSI).
 * 
 * @author Ilya Markov
 * 
 * @see "Relevant document distribution estimation method for resource selection",
 * 		Luo Si and Jamie Callan.
 * 		In <i>Proceedings of SIGIR</i>, pages 298-305, 2003.
 */
public interface ResourceSelection {
	/**
	 * Given a list of scored documents and a list of corresponding resources,
	 * calculates a score for each resource and ranks resources by their scores.
	 * Returns an ordered list of scored resources.
	 * Resources are ordered descending with respect to their scores.
	 * 
	 * <p>
	 * Each resource in the <code>resources</code> list
	 * should correspond to a document in the <code>documents</code> list,
	 * i.e. <code>resources.get(i)</code> is a resource of <code>documents.get(i)</code>.
	 * Therefore, the lists should be of the same length.
	 * </p>
	 * 
	 * <p>
	 * Documents should not be necessarily sorted by their scores.
	 * </p>
	 * 
	 * @param documents The list of scored documents.
	 * @param resources The list of corresponding resources.
	 * 
	 * @return A descending ordered list of scored resources.
	 * 
	 * @throws NullPointerException
	 * 		if <code>documents</code> or <code>resources</code>
	 * 		is <code>null</code>.
	 * @throws IllegalArgumentException
	 * 		if <code>documents</code> and <code>resources</code>
	 * 		are of different size.
	 */
	<T> List<ScoredEntity<Resource>> select(List<ScoredEntity<T>> documents,
			List<Resource> resources);
}
