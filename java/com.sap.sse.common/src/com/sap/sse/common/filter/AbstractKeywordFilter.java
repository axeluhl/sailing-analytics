package com.sap.sse.common.filter;

import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.common.filter.impl.KeywordMatcher;

/**
 * {@link Filter} implementation that matches the given objects using a {@link KeywordMatcher}.
 *
 * @param <T>
 */
public abstract class AbstractKeywordFilter<T> implements Filter<T> {

    private final KeywordMatcher<T> matcher = new KeywordMatcher<T>() {
        @Override
        public Iterable<String> getStrings(T t) {
            return AbstractKeywordFilter.this.getStrings(t);
        }
    };

    private final Set<String> keywords = new HashSet<>();

    /**
     * Sets the keywords to use for matching of domain objects.
     */
    public void setKeywords(Iterable<String> keywords) {
        this.keywords.clear();
        Util.addAll(keywords, this.keywords);
    }

    /**
     * @see KeywordMatcher#getStrings(Object)
     */
    public abstract Iterable<String> getStrings(T t);

    @Override
    public boolean matches(T t) {
        return matcher.matches(keywords, t);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
