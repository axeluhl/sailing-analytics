package com.sap.sse.common.search;

public class KeywordQuery implements Query {
    private final Iterable<String> keywords;

    protected KeywordQuery(Iterable<String> keywords) {
        super();
        this.keywords = keywords;
    }

    public Iterable<String> getKeywords() {
        return keywords;
    }
}
