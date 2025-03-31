package com.sap.sse.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import com.sap.sse.common.Util.Pair;

/**
 * A time window of {@link Timed} objects, constructed from an {@link Iterator} that is expected to deliver the objects
 * in time-wise ascending order (from early to late), that is an {@link Iterator} producing all {@link Pair}s of the
 * objects within one window such that the first pair's component is earlier than or at the same time point as the
 * pair's second element, and in case of objects A and B with equal time stamp only one of (A, B) and (B, A) being part
 * of the resulting {@link Iterator} represented by this object.
 * <p>
 * 
 * An optional equivalence definition for the {@code T} elements can be provided, allowing only for non-equivalent
 * objects to be in the time window. This can help eliminating duplicates.<p>
 * 
 * Instances of this data structure are not thread-safe. Callers need to take care of synchronizing concurrent calls to
 * methods of the same instance.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public class TimeWindow<T extends Timed> implements Iterator<Pair<T, T>> {
    private final Iterator<T> iterator;
    
    /**
     * The maximum duration between first and last element in {@link #content}, inclusive.
     */
    private final Duration length;
    
    /**
     * Time-ordered elements, earliest first; the maximum duration between first and last does not exceed
     * {@link #length}. After construction and after a call to any public method on objects of this type return,
     * the list is guaranteed to contain all elements delivered by the {@link #iterator} that fit the {@link #length}
     * constraint. If only one element is left, no more {@link Pair} can be constructed as output and the result
     * iteration ends, so {@link #hasNext()} returns {@code false} then.
     */
    private final LinkedList<T> content;
    
    /**
     * An element obtained from the {@link #iterator} that wouldn't fit into {@link #contents} without violating the
     * {@link #length} constraint. Only after removing one or more elements at the window's beginning may this candidate
     * fit. After object construction and after any public method has returned, this field is guaranteed to be
     * non-{@code null} if and only if another element was available from the {@link #iterator} that didn't fit the
     * {@link #length} constraint. In other words, if this field is {@code null}, no more elements can be added to the
     * {@link #content} list.
     */
    private T nextCandidate;
    
    /**
     * Pairs are constructed from the first {@link #content} element and other elements from the window.
     * We do this in ascending order, starting after the first, so at index 1. Should {@link #content}
     * contain only one element, this index points behind the end. If the index points behind the end,
     * all pairs with the same first element have been constructed for the current window's contents,
     * the first element can be popped from the list, and more elements can be filled if they fit the
     * length restriction.
     */
    private Iterator<T> nextSecondPairElement;
    
    /**
     * If not {@code null}, objects are only permitted into the {@link #content} list if no other object
     * in the list has an equivalence wrapper equal to that of the object to be permitted into the list.
     * In other words, this defines an equivalence relation based on which uniqueness in {@link #content}
     * is guaranteed. Note that the {@link Object}s retured must have a {@link Object#hashCode()} implementation
     * that is consistent with the {@link Object#equals(Object)} definition.
     */
    private final Function<T, Object> equivalenceWrapperSupplier;
    
    /**
     * If {@link #equivalenceWrappersForContent} is {@code null} then so is this field. Otherwise, this field
     * holds the set of all the equivalence wrapper objects for all objects in {@link #content} which is then
     * used to ensure uniqueness of the elements in {@link #content} with regards to the equivalence relation
     * defined through {@link #equivalenceWrapperSupplier}.
     */
    private final Set<Object> equivalenceWrappersForContent;
    
    public TimeWindow(Iterator<T> iterator, Duration length) {
        this(iterator, length, /* equivalenceWrapperSupplier */ null);
    }
    
    public TimeWindow(Iterator<T> iterator, Duration length, Function<T, Object> equivalenceWrapperSupplier) {
        this.equivalenceWrapperSupplier = equivalenceWrapperSupplier;
        this.equivalenceWrappersForContent = equivalenceWrapperSupplier == null ? null : new HashSet<>();
        this.iterator = iterator;
        this.length = length;
        content = new LinkedList<>();
        nextCandidate = iterator.hasNext() ? iterator.next() : null;
        fill();
    }

    /**
     * Fills up {@link #content} as far as possible given the {@link #length} constraint, starting
     * with {@link #nextCandidate} and continuing to take elements from the {@link #iterator}.
     * If it is possible to fill at least two elements from anywhere in the remaining sequence
     * produced by {@link #iterator} then at least two elements will end up in {@link #content}.
     * When more than one element ends up in {@link #content} then the sequence is guaranteed to
     * be maximized with regards to the {@link #length} restriction, so either there is no more
     * candidate to add or the {@link #nextCandidate} won't {@link #candidateFits(Timed) fit}.
     */
    private void fill() {
        T candidate = nextCandidate;
        if (candidate != null) {
            do {
                if (content.size() < 2) {
                    // if another candidate exists, keep removing elements from the beginning of the list
                    // until the candidate fits:
                    while (!candidateFits(candidate)) {
                        removeFirstFromContent();
                    }
                }
                // now try to fill in more elements that fit; if we end up with only
                // one element in content, remove it again and start over with the next
                // candidate
                while (candidate != null && candidateFits(candidate)) {
                    add(candidate);
                    candidate = iterator.hasNext() ? iterator.next() : null;
                }
            } while (content.size() < 2 && candidate != null);
        }
        nextCandidate = candidate;
        nextSecondPairElement = content.size() >= 2 ? content.listIterator(1) : null;
    }

    /**
     * doesn't add {@code candidate} if by an equivalence relation provided the candidate is already part of {@link #content}.
     */
    private void add(T candidate) {
        if (equivalenceWrapperSupplier == null || equivalenceWrappersForContent.add(equivalenceWrapperSupplier.apply(candidate))) {
            content.add(candidate);
        }
    }
    
    private void pop() {
        removeFirstFromContent();
        fill();
    }

    private void removeFirstFromContent() {
        final T elementRemoved = content.removeFirst();
        if (equivalenceWrapperSupplier != null) {
            equivalenceWrappersForContent.remove(equivalenceWrapperSupplier.apply(elementRemoved));
        }
    }

    private boolean candidateFits(T candidate) {
        return content.isEmpty() || content.getFirst().getTimePoint().until(candidate.getTimePoint()).compareTo(length) <= 0;
    }

    @Override
    public boolean hasNext() {
        return nextSecondPairElement != null && nextSecondPairElement.hasNext();
    }

    @Override
    public Pair<T, T> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Pair<T, T> result = new Pair<>(content.getFirst(), nextSecondPairElement.next());
        if (!nextSecondPairElement.hasNext()) {
            // the last element from content was used for a Pair; pop first and fill again
            pop();
        }
        return result;
    }
}
