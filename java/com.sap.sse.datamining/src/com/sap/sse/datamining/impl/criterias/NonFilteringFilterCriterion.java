package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriterion;

public class NonFilteringFilterCriterion<T> implements FilterCriterion<T> {

    @Override
    public boolean matches(T element) {
        return true;
    }

}
