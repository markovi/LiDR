/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

//import ch.usi.inf.ir.merging.*;
import ch.usi.inf.lidr.norm.*;
import ch.usi.inf.lidr.utils.ScoredEntity;


/**
 * @author Ilya Markov
 */
@RunWith(Parameterized.class)
public class NormalizationTest {
	private static final double[][] SCORES = new double[][] {
		{},
		{29, 15, 10, 6, 4, 3, 1},
		{6.15, 5.72, 2.23, -1.02, -2.232, -10.042},
		{1, 1, 1, 1, 1, 1},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{1, 1, 1, 1, 0, 0, 0, 0}};
	
	private static final LinearScoreNormalization[] LINEAR_NORMALIZATION = new LinearScoreNormalization[] {
		new Identity(),
		new MinMax(),
		new ZScore(),
		new Sum(),
	};
	
	private static final int RANK_CUTOFF = 10;
	
	@Parameters
	public static Collection<Object[]> normMethods() {
		List<List<ScoredEntity<Object>>> scoreLists = new ArrayList<List<ScoredEntity<Object>>>(SCORES.length);
		for (int i = 0; i < SCORES.length; i++) {
			List<ScoredEntity<Object>> scoreList = new ArrayList<ScoredEntity<Object>>(SCORES[i].length);
			
			for (int j = 0; j < SCORES[i].length; j++) {
				scoreList.add(new ScoredEntity<Object>(j, SCORES[i][j]));
			}
			
			scoreLists.add(scoreList);
		}
		
		Collection<Object[]> params = new ArrayList<Object[]>();
		
		for (int i = 0; i < LINEAR_NORMALIZATION.length; i++) {
			LINEAR_NORMALIZATION[i].setRankCutoff(RANK_CUTOFF);
			
			for (int j = 0; j < scoreLists.size(); j++) {
				params.add(new Object[]{LINEAR_NORMALIZATION[i], scoreLists.get(j)});
			}
		}
		
		return params;
	}
	
	
	private final ScoreNormalization normalization;
	private final List<ScoredEntity<Object>> scoredDocs;
	
	public NormalizationTest(ScoreNormalization normalization, List<ScoredEntity<Object>> scoredDocs) {
		this.normalization = normalization;
		this.scoredDocs = scoredDocs;
	}
	
	@Test
	public void normPreservesDocIds() {
		List<ScoredEntity<Object>> normScoredDocs = normalization.normalize(scoredDocs);
		
		for (int i = 0; i < normScoredDocs.size(); i++) {
			assertEquals(scoredDocs.get(i).getEntity(), normScoredDocs.get(i).getEntity());
		}
	}

	@Test
	public void normPreservesOrder() {
		List<ScoredEntity<Object>> normScoredDocs = normalization.normalize(scoredDocs);
		
		for (int i = 1; i < normScoredDocs.size(); i++) {
			assertTrue((scoredDocs.get(i - 1).getScore() >= scoredDocs.get(i).getScore())
					== (normScoredDocs.get(i - 1).getScore() >= normScoredDocs.get(i).getScore()));
		}
	}
	
	@Test
	public void resultLengthEqualsOriginalLength() {
		assertEquals(scoredDocs.size(), normalization.normalize(scoredDocs).size());
	}
	
	@Test(expected=NullPointerException.class)
	public void normWithNullArrayThrowsNPE() {
		normalization.normalize(null);
	}
}
