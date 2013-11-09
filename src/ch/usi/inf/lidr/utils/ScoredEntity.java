/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (C) 2013  Ilya Markov
 * 
 * A wrapper for an entity and its score.
 * 
 * <p>
 * <b>NOTE:</b> this class has a natural ordering that is inconsistent with equals!
 * In particular, two scored entities are equal
 * if and only if their corresponding entities are equal,
 * while the natural ordering is determined by entity scores.
 * </p>
 * 
 * @author Ilya Markov
 */
public final class ScoredEntity<T> implements Comparable<ScoredEntity<T>> {
	
	/**
	 * Sorts a given list of scored entities with respect
	 * to their scores in a direction specified by <code>isAscending</code>.
	 * 
	 * @param <T> The type of scored entities.
	 * @param scoredEntities The list of unsorted scored entities.
	 * @param isAscending Set to <code>true</code> for ascending sorting.
	 * 		Otherwise, set to <code>false</code>.
	 * 
	 * @return The list of scored entities sorted with respect
	 * 		to their scores in a specified direction.
	 * 
	 * @throws NullPointerException
	 * 		if <code>scoredEntities</code> is <code>null</code>.
	 */
	public static <T> List<ScoredEntity<T>> sort(List<ScoredEntity<T>> scoredEntities,
			boolean isAscending)
	{
		if (scoredEntities == null) {
			throw new NullPointerException("The list of scored entities is null.");
		}
		
		List<ScoredEntity<T>> sortedEntities = new ArrayList<ScoredEntity<T>>();
		sortedEntities.addAll(scoredEntities);
		Collections.sort(sortedEntities);
		if (!isAscending) {
			Collections.reverse(sortedEntities);
		}
		
		return sortedEntities;
	}
	
	
	/**
	 * The entity.
	 * 
	 * @see #entity
	 */
	private final T entity;
	/**
	 * The score.
	 * 
	 * @see #score
	 */
	private final double score;
	
	/**
	 * Constructs a scored entity out of
	 * a given <code>entity</code> and a <code>score</code>.
	 * 
	 * @param entity The entity.
	 * @param score The score.
	 * 
	 * @throws NullPointerException
	 * 		if the <code>entity</code> is <code>null</code>.
	 */
	public ScoredEntity(T entity, double score) {
		if (entity == null) {
			throw new NullPointerException("The entity is null.");
		}
		
		this.entity = entity;
		this.score = score;
	}

	/**
	 * Returns the entity.
	 * 
	 * @return The entity.
	 */
	public T getEntity() {
		return entity;
	}
	
	/**
	 * Returns the score.
	 * 
	 * @return The score.
	 */
	public double getScore() {
		return score;
	}

	/**
	 * <b>NOTE:</b> two scored entities are equal
	 * if and only if their corresponding entities are equal.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object scoredEntity) {
		return (scoredEntity instanceof ScoredEntity<?>)
			&& getEntity().equals(((ScoredEntity<?>) scoredEntity).getEntity());
	}

	/**
	 * <b>NOTE:</b> returns the hash code of the corresponding entity.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getEntity().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return entity.toString() + ":" + (Math.round(score * 100) / (double) 100);
	}

	/**
	 * <b>NOTE:</b> the natural ordering of scored entities
	 * is determined by their scores. 
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ScoredEntity<T> scoredEntity) {
		if (!(scoredEntity instanceof ScoredEntity<?>)) {
			throw new RuntimeException("Object are incomparable.");
		}
		
		double entityScore = ((ScoredEntity<?>) scoredEntity).getScore();
		if (getScore() == entityScore) {
			return 0;
		}
		
		return getScore() > entityScore ? 1 : -1;
	}
}
