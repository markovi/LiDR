/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.merging;

import ch.usi.inf.lidr.norm.ScoreNormalization;

/**
 * An interface for results merging methods.
 * Note that results merging techniques perform
 * score normalization with a help of a centralized sample index (CSI).
 * 
 * @author Ilya Markov
 * 
 * @see "Advances in Information Retrieval, chapter 5. Distributed Information Retrieval",
 * 		Jamie Callan.
 * 		<i>Kluwer Academic Publishers</i>, pages 127-150, 2000.
 * @see "Federated Search",
 * 		Milad Shokouhi and Luo Si.
 * 		<i>Foundations and Trends in Information Retrieval</i>, pages 1-102, 2011.
 */
public interface ResultsMerging extends ScoreNormalization {

}
