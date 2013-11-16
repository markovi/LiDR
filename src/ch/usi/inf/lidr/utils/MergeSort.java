/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The merge sort.
 * 
 * @author Ilya Markov
 */
public final class MergeSort {
	/**
     * Mergesort algorithm for lists.
     * Assumes that the lists are already sorted in descending order.
     * 
     * @param a The list of {@link Comparable} items.
     * @param b The list of {@link Comparable} items.
     * 
     * @throws NullPointerException
     * 		if <code>a</code> or <code>b</code> is <code>null</code>.
     */
	public static <T extends Comparable<T>> List<T> sort(List<T> a, List<T> b) {
		if ((a == null) || (b == null)) {
			throw new NullPointerException("The list to sort is null.");
		}
		
        List<T> result = new LinkedList<T>();
        
        int indexA = 0;
        int indexB = 0;
        
        while((indexA < a.size()) && (indexB < b.size())) {
        	result.add((a.get(indexA).compareTo(b.get(indexB)) > 0) ?
        			a.get(indexA++) : b.get(indexB++));
		}
        
        while (indexA < a.size()) {
        	result.add(a.get(indexA++));
        }
        
        while (indexB < b.size()) {
        	result.add(b.get(indexB++));
        }
        
        return result;
    }
	
    /**
     * Mergesort algorithm for arrays.
     * Assumes that the arrays are already sorted in descending order.
     * 
     * @param a The array of {@link Comparable} items.
     * @param b The array of {@link Comparable} items.
     * 
     * @throws NullPointerException
     * 		if <code>a</code> or <code>b</code> is <code>null</code>.
     */
	public static <T extends Comparable<T>> T[] sort(T[] a, T[] b) {
		if ((a == null) || (b == null)) {
			throw new NullPointerException("The array to sort is null.");
		}
		
		return sort(Arrays.asList(a), Arrays.asList(b)).toArray(a);
    }
}
