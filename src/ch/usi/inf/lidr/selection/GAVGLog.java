/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

/**
 * The geometric average blog distillation method
 * proposed by Seo and Croft
 * and used for distributed retrieval by Arguello et al.
 * Calculates resources score as a product
 * of document relevance scores.
 * 
 * <p>
 * <b>NOTE:</b> here GAVG is implemented as a sum of logs
 * of document relevance scores.
 * </p>
 * 
 * @author Ilya Markov
 * 
 * @see "Blog site search using resource selection",
 * 		Jangwon Seo and Bruce W. Croft.
 * 		In <i>Proceedings of CIKM</i>, pages 1053--1062, 2008.
 * @see "Classification-based resource selection",
 * 		Jaime Arguello, Jamie Callan and Fernando Diaz.
 * 		In <i>Proceedings of CIKM</i>, pages 1277-1286, 2009.
 */
public final class GAVGLog extends ReDDE {
	/**
	 * @see ch.usi.inf.lidr.selection.ReDDE#getScoreAtRank(double, int)
	 */
	@Override
	protected double getScoreAtRank(double score, int rank) {
		return Math.log(score);
	}
}
