package com.sap.sse.gwt.dispatch.shared.commands;

import java.util.ArrayList;
import java.util.Collection;

public class ListResult<T extends DTO> extends AbstractCollectionResult<ArrayList<T>, T> {
    
    public ListResult() {
        super(new ArrayList<T>(), null);
    }

    public ListResult(Collection<T> values) {
        super(new ArrayList<T>(), values);
    }
}
