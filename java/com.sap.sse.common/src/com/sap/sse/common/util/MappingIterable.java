package com.sap.sse.common.util;

import java.util.Iterator;

import com.sap.sse.common.util.MappingIterator.MapFunction;

public class MappingIterable<S, T> implements Iterable<T> {
    private final Iterable<S> iterable;
    private final MapFunction<S, T> mapper;
    
    public MappingIterable(Iterable<S> iterable, MapFunction<S, T> mapper) {
        super();
        this.iterable = iterable;
        this.mapper = mapper;
    }

    @Override
    public Iterator<T> iterator() {
        return new MappingIterator<S, T>(iterable.iterator(), mapper);
    }
}
