package com.sap.sse.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sse.common.Util;

public class KeywordQuery implements Query {
    private final Iterable<String> keywords;

    public KeywordQuery(String... keywords) {
        this.keywords = trim(Arrays.asList(keywords));
    }
    
    private Iterable<String> trim(Iterable<String> keywords) {
        final List<String> result = new ArrayList<String>(Util.size(keywords));
        for (String keyword : keywords) {
            result.add(keyword.trim());
        }
        return result;
    }

    public KeywordQuery(Iterable<String> keywords) {
        super();
        this.keywords = trim(keywords);
    }

    public Iterable<String> getKeywords() {
        return keywords;
    }
    
    @Override
    public String toString() {
        return ""+keywords;
    }
}
