package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriteria;

public class NonFilteringFilterCriteria<T> implements FilterCriteria<T> {

    @Override
    public boolean matches(T element) {
        return true;
    }

}
