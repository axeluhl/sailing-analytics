package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.datamining.SelectionCriteria;

public abstract class AbstractSelectionCriteria<T> implements SelectionCriteria {
    
    private List<T> selection;
    
    public AbstractSelectionCriteria(Collection<T> selection) {
        this.selection = new ArrayList<T>(selection);
    }

    public Collection<T> getSelection() {
        return selection;
    }

}
