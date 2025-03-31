package com.sap.sse.common.filter;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.filter.impl.KeywordMatcher;

/**
 * Matches a list of keywords in a list of strings. A match is recognized if each of the keywords is contained in at least
 * one of the {@link #getStrings(Object) strings}. The typical use for this class is building a disjuctive ("AND"-like)
 * keyword matcher where {@link #getStrings(Object)} provides one or more strings extracted from the searchable object
 * and its context.
 * 
 * @author Nicolas Klose
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public abstract class AbstractListFilter<T> {
    
    private final KeywordMatcher<T> matcher = new KeywordMatcher<T>() {
        @Override
        public Iterable<String> getStrings(T t) {
            return AbstractListFilter.this.getStrings(t);
        }
    };

    /**
     * @see KeywordMatcher#getStrings(Object)
     */
    public abstract Iterable<String> getStrings(T t);
    
    /**
     * Constructs a list based on the contents of {@code all} using the {@link #matcher} which matches
     * the {@code keywords} against what {@link #getStrings(Object)} returns for the respective
     * object from {@code all}.
     */
    public Iterable<T> applyFilter(Iterable<String> keywords, Iterable<T> all) {
        List<T> result = new ArrayList<T>();
        if (all != null) {
            for (T t : all) {
                if (matcher.matches(keywords, t)) {
                    result.add(t);
                }
            }
        }
        return result;
    }
}

