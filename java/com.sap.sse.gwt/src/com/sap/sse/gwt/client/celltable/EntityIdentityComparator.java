package com.sap.sse.gwt.client.celltable;

/**
 * An entity can change its state over time while remaining the same. Objects such as DTOs (data transfer objects)
 * represent the state of an entity at a given point in time, so two such DTOs may represent different states of the
 * same entity. These copied may not be equal due to their different state. So, e.g., a UI that needs to decide whether
 * a refresh is needed would rely on this regular definition of state-based equality.
 * <p>
 * 
 * However, different, non-equal objects may still represent the same entity. This equivalence can become important, e.g., for
 * selection management where in a UI widget a new and different copy of the same entity is being displayed and an old copy
 * was in the selection.<p>
 * 
 * Implementors of this interface are expected to define under which conditions two objects represent the <em>same</em>
 * entity. This can, e.g., be decided based on an ID field if the object type offers one.
 * 
 * @author Lukas Furmanek
 *
 * @param <T> type of the entity representation, such as a DTO class
 */
public interface EntityIdentityComparator<T> {
    /**
     * Compares the objects and returns <code>true</code> if the objects represent the same entity.
     * Note that this does not imply that they are also {@link #equals(Object) equal}.
     */
    boolean representSameEntity(T dto1, T dto2);
    
    /**
     * A hash code that is "in line" with the equality definition implemented by
     * {@link #representSameEntity(Object, Object)}. More specifically, two objects that represent the same entity
     * according to {@link #representSameEntity(Object, Object)} must have equal hash codes. Conversely, two objects
     * with equal hash codes may still represent different entities.
     */
    int hashCode(T t);
}