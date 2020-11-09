package com.sap.sse.common.filter.impl;

import com.sap.sse.common.Util;

/**
 * Matches domain objects with a set of keywords. A match is recognized if each of the keywords is contained in at
 * least one of the {@link #getStrings(Object) strings}. The typical use for this class is building a disjuctive
 * ("AND"-like) keyword matcher where {@link #getStrings(Object)} provides one or more strings extracted from the
 * searchable object and its context.
 *
 * @param <T>
 */
public abstract class KeywordMatcher<T> {

    /**
     * Subclasses must implement this to extract the strings from an object of type <code>T</code> based on which the
     * filter performs its filtering
     * 
     * @param t
     *            the object from which to extract the searchable strings
     * @return the searchable strings
     */
    public abstract Iterable<String> getStrings(T t);

    public boolean matches(Iterable<String> keywords, T t) {
        boolean matchesOrNoKeywords = keywords == null || Util.isEmpty(keywords) || containsText(t, keywords);
        return matchesOrNoKeywords;
    }

    public boolean matchesExactly(Iterable<String> keywords, T t) {
        return keywords != null && matches(t, keywords, true);
    }
    
    private boolean containsText(T obj, Iterable<String> keywords) {
        return matches(obj, keywords, false);
    }
    
    /**
     * Returns <code>true</code> if for each of the <code>keywords</code> any of the {@link #getStrings(Object) values
     * to check} contains that keyword.
     * 
     * @param keywords
     *            the words to filter on
     * @param exactMatch
     *            returns <code>true</code> only if a string matches exactly (still ignores differences in upper/lower case letters)
     * @return <code>true</code> if the <code>valuesToCheck</code> contains all <code>wordsToFilter</code>,
     *         <code>false</code> if not
     */
    private boolean matches(T obj, Iterable<String> keywords, boolean exactMatch) {
        boolean failed = false;
        for (String word : keywords) {
            String textAsUppercase = word.toUpperCase();
            failed = true;
            for (String s : getStrings(obj)) {
                if (matches(s, textAsUppercase, exactMatch)) {
                    failed = false;
                    break;
                }
            }
            if (failed) {
                return false;
            }
        }
        return true;
    }
    
    private boolean matches(String s, String keyword, boolean exactMatch) {
        if (s == null) {
            return false;
        }
        
        return exactMatch ? s.equalsIgnoreCase(keyword) : s.toUpperCase().contains(keyword);
    }
}
