package com.sap.sse.gwt.dispatch.shared.commands;

import java.util.Collection;
import java.util.TreeSet;

public class SortedSetResult<T extends DTO> extends AbstractCollectionResult<TreeSet<T>, T> {
    public SortedSetResult() {
        super(new TreeSet<T>(), null);
    }

    public SortedSetResult(Collection<T> values) {
        super(new TreeSet<T>(), values);
    }
}
