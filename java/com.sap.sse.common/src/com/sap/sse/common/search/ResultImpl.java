package com.sap.sse.common.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public  class ResultImpl<H extends Hit> implements Result<H> {
    private final TreeSet<H> hits;
    private final Query query;
    
    public ResultImpl(Query query, Comparator<H> ranker) {
        hits = new TreeSet<H>(ranker);
        this.query = query;
    }
    
    public Iterable<H> getHits() {
        return Collections.unmodifiableCollection(hits);
    }
    
    public void addHit(H hit) {
        hits.add(hit);
    }

    @Override
    public Query getQuery() {
        return query;
    }
}
