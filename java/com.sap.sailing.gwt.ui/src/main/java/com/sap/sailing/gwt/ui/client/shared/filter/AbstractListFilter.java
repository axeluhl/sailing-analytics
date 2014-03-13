package com.sap.sailing.gwt.ui.client.shared.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractListFilter<T> {

    /**
     * Subclasses must implement this to extract the strings from an object of type <code>T</code> based on which the
     * filter performs its filtering
     * 
     * @param t
     *            the object from which to extract the searchable strings
     * @return the searchable strings
     */
    public abstract Iterable<String> getStrings(T t);
    
    
    /**
     * Constructs a list based on the contents of {@link #all} and the current search phrase {@link #text}. 
     */
    public List<T> applyFilter(String text, Iterable<T> all) {
        List<String> inputText = Arrays.asList(text.split(" "));
        List<T> sortedList = new ArrayList<T>();
        if (all != null) {
            if (inputText != null && !inputText.isEmpty()) {
                for (T t : all) {
                    if (containsText(t, inputText)) {
                        sortedList.add(t);
                    }
                }
            } else {
                for (T t : all) {
                    sortedList.add(t);
                }
            }
        }
        return sortedList;
    }
    
    /**
     * Returns <code>true</code> if <code>wordsToFilter</code> contain a value of the <code>valuesToCheck</code>
     * 
     * @param wordsToFilter
     *            the words to filter on
     * @param valuesToCheck
     *            the values to check for.
     * @return <code>true</code> if the <code>valuesToCheck</code> contains all <code>wordsToFilter</code>,
     *         <code>false</code> if not
     */
    private boolean containsText(T obj, List<String> wordsToFilter) {
        boolean failed = false;
        for (String word : wordsToFilter) {
            String textAsUppercase = word.toUpperCase().trim();
            failed = true;
            for (String s : getStrings(obj)) {
                if (s != null && s.toUpperCase().contains(textAsUppercase)) {
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
}

