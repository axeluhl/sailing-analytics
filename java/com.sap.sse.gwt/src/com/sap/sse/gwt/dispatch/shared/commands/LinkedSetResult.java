package com.sap.sse.gwt.dispatch.shared.commands;

import java.util.Collection;
import java.util.LinkedHashSet;

public class LinkedSetResult<T extends DTO> extends AbstractCollectionResult<LinkedHashSet<T>, T> {

    public LinkedSetResult() {
        super(new LinkedHashSet<T>(), null);
    }

    public LinkedSetResult(Collection<T> values) {
        super(new LinkedHashSet<T>(), values);
    }
}
