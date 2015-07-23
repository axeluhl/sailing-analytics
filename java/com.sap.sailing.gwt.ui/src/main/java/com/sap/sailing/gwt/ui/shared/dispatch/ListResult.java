package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.ArrayList;
import java.util.List;

public class ListResult<T extends DTO> implements CollectionResult<T> {
    private ArrayList<T> values = new ArrayList<>();
    
    @SuppressWarnings("unused")
    private ListResult() {
    }

    public ListResult(List<T> values) {
        super();
        this.values.addAll(values);
    }
    
    public List<T> getValues() {
        return values;
    }
}
