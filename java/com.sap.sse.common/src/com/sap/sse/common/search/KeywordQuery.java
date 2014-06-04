package com.sap.sse.common.search;

import java.util.Arrays;

public class KeywordQuery implements Query {
    private final Iterable<String> keywords;

    public KeywordQuery(String... keywords) {
        this.keywords = Arrays.asList(keywords);
    }
    
    public KeywordQuery(Iterable<String> keywords) {
        super();
        this.keywords = keywords;
    }

    public Iterable<String> getKeywords() {
        return keywords;
    }
}
