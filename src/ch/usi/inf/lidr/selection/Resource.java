/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.selection;

/**
 * A resource representation that is used
 * in resource selection methods.
 * 
 * @author Ilya Markov
 */
public final class Resource {
	/**
	 * The resource id. Can be any object.
	 */
	private final Object resourceId;
	/**
	 * The full size of the resource.
	 */
	private final int size;
	/**
	 * The size of a sample used to represent the resource.
	 */
	private final int sampleSize;
	
	/**
	 * Creates a resources out of given parameters.
	 * 
	 * @param resourceId The resource id. Can be any object.
	 * @param fullSize The size of the resource.
	 * @param sampleSize The size of a sample used to represent the resource.
	 * 
	 * @throws NullPointerException
	 * 		if the <code>resource</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 * 		if the <code>fullSize</code> or <code>sampleSize</code>
	 * 		is less than or equal to zero.
	 */
	public Resource(Object resourceId, int fullSize, int sampleSize) {
		if (resourceId == null) {
			throw new NullPointerException("The resource id is null.");
		}
		if (fullSize <= 0) {
			throw new IllegalArgumentException("The resource size is less or equal to zero: " + fullSize);
		}
		if (sampleSize <= 0) {
			throw new IllegalArgumentException("The resource sample size is less or equal to zero: " + sampleSize);
		}
		
		this.resourceId = resourceId;
		this.size = fullSize;
		this.sampleSize = sampleSize;
	}

	/**
	 * Returns the resource id.
	 * 
	 * @return The resource id.
	 * 
	 * @see #resourceId
	 */
	public Object getResourceId() {
		return resourceId;
	}

	/**
	 * Returns the size of the resource.
	 * 
	 * @return The resource size.
	 * 
	 * @see #size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns the size of a sample.
	 * 
	 * @return The size of a sample.
	 * 
	 * @see #sampleSize
	 */
	public int getSampleSize() {
		return sampleSize;
	}

	/**
	 * <b>NOTE:</b> two resources are equal if and only if their
	 * resource ids are equal.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Resource)
			&& (getResourceId().equals(((Resource) obj).getResourceId()));
	}

	/**
	 * <b>NOTE:</b> returns the hash code of the resource id.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return resourceId.hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return resourceId.toString();
	}
}
