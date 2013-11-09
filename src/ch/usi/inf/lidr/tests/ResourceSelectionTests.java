/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.usi.inf.lidr.selection.*;
import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * Tests for {@link ResourceSelection}.
 * 
 * @author Ilya Markov
 */
@RunWith(Parameterized.class)
public class ResourceSelectionTests {
	private static List<ScoredEntity<Object>> documents;
	private static List<Resource> resources;
	private static List<Resource> uniqueResources;
	
	@BeforeClass
	public static void initInput() {
		documents = new LinkedList<ScoredEntity<Object>>();
		resources = new LinkedList<Resource>();
		uniqueResources = new ArrayList<Resource>();
		
		int numResources = 7;
		for (int i = 0; i < numResources; i++) {
			uniqueResources.add(new Resource(i, (i + 1) * 1561, (i + 1) * 98));
		}
		
		int numDocuments = 1643;
		Random random = new Random(1234);
		for (int i = 0; i < numDocuments; i++) {
			double score = Math.log(i + 1) * 13.2 / (i + 1);
			documents.add(new ScoredEntity<Object>(i, score));
			
			int resourceIndex = random.nextInt(numResources);
			resources.add(uniqueResources.get(resourceIndex));
		}
	}
	
	@Parameters
	public static java.util.Collection<Object[]> resourceSelectionMethods() {
		List<Object[]> params = new ArrayList<Object[]>();
		
		params.add(new Object[]{new ReDDE()});
		params.add(new Object[]{new ReDDETop()});
		params.add(new Object[]{new CRCSExp()});
		params.add(new Object[]{new CRCSLinear()});
		params.add(new Object[]{new ReDDETop()});
		params.add(new Object[]{new GAVGLog()});
		params.add(new Object[]{new SUSHI()});
		params.add(new Object[]{new CiSS()});
		
		return params;
	}
	
	/**
	 * A resource selection method under test.
	 */
	private final ResourceSelection selection;

	/**
	 * Creates a test suite for a given resource selection method.
	 * 
	 * @param selection The resource selection method.
	 * 
	 * @throws NullPointerException
	 * 		if <code>resourceSelection</code> is <code>null</code>.
	 */
	public ResourceSelectionTests(ResourceSelection selection) {
		if (selection == null) {
			throw new NullPointerException("Resource selection method should not be null.");
		}
		
		this.selection = selection;
	}

	/**
	 * Tests that all resources passed to
	 * {@link ResourceSelection#select(java.util.List, java.util.List)}
	 * appear in the result of the method call.
	 */
	@Test
	public void testScoredResourcesEqualInputResources() {
		List<ScoredEntity<Resource>> scoredResources = selection.select(documents, resources);
		
		Set<Resource> actualResources = new HashSet<Resource>();
		for (ScoredEntity<Resource> scoredResource : scoredResources) {
			actualResources.add(scoredResource.getEntity());
		}
		
		Set<Resource> expectedResources = new HashSet<Resource>();
		for (Resource resource : uniqueResources) {
			expectedResources.add(resource);
		}
		
		assertEquals(expectedResources, actualResources);
	}
	
	/**
	 * Tests that scored resources are ordered descending.
	 */
	@Test
	public void testDescendingSorting() {
		List<ScoredEntity<Resource>> scoredResources = selection.select(documents, resources);
		
		System.out.println("Class to test: " + selection.getClass().getName());
		System.out.println("Ranking of resources: " + scoredResources);
		
		for (int i = 0; i < scoredResources.size() - 1; i++) {
			assertTrue(scoredResources.get(i).getScore() >= scoredResources.get(i + 1).getScore());
		}
	}
	
	/**
	 * Tests that resources scores are valid doubles,
	 * i.e. neither an infinity nor NaN.
	 */
	@Test
	public void testValidScores() {
		List<ScoredEntity<Resource>> scoredResources = selection.select(documents, resources);
		
		double scoreSum = 0;
		for (ScoredEntity<Resource> scoredResource : scoredResources) {
			assertFalse(((Double)scoredResource.getScore()).isNaN());
			assertFalse(((Double)scoredResource.getScore()).isInfinite());
			scoreSum += scoredResource.getScore();
		}
		assertTrue(scoreSum > 0);
	}
	
	/**
	 * Tests that {@link ResourceSelection#select(java.util.List, java.util.List)}
	 * called with lists of different length
	 * throws {@link IllegalArgumentException}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testDifferentLengthInput() {
		selection.select(documents, uniqueResources);
	}

	/**
	 * Tests that {@link ResourceSelection#select(java.util.List, java.util.List)}
	 * called with a <code>null</code> list throws {@link NullPointerException}.
	 */
	@Test(expected=NullPointerException.class)
	public void testNullInput() {
		selection.select(null, resources);
	}
}
