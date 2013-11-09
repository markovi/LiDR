/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.usi.inf.lidr.selection.Resource;

/**
 * Tests for {@link Resource}.
 * 
 * @author Ilya Markov
 */
public class ResourceTests {
	private static final String resourceId = "test resource";
	private static final int size = 1234;
	private static final int sampleSize = 300;
	private static final Resource resource = new Resource(resourceId, size, sampleSize);
	

	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(resourceId.hashCode(), resource.hashCode());
	}

	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#Resource(java.lang.Object, int, int)}.
	 */
	@Test(expected=NullPointerException.class)
	public void testResourceNPE() {
		new Resource(null, size, sampleSize);
	}
	
	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#Resource(java.lang.Object, int, int)}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testResourceNegativeSize() {
		new Resource(resourceId, -1, sampleSize);
	}
	
	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#Resource(java.lang.Object, int, int)}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testResourceNegativeSampleSize() {
		new Resource(resourceId, size, -5);
	}

	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#getResourceId()}.
	 */
	@Test
	public void testGetResourceId() {
		assertEquals(resourceId, resource.getResourceId());
	}

	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#getSize()}.
	 */
	@Test
	public void testGetSize() {
		assertEquals(size, resource.getSize());
	}

	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#getSampleSize()}.
	 */
	@Test
	public void testGetSampleSize() {
		assertEquals(sampleSize, resource.getSampleSize());
	}

	/**
	 * Test method for {@link ch.usi.inf.lidr.selection.Resource#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Resource otherResource = new Resource(resourceId, 100, 10);
		assertTrue(resource.equals(otherResource));
	}

}
