package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriterion;

public class OrCompoundFilterCriterion<DataType> extends CompoundFilterCriterion<DataType> {

    @Override
    public boolean matches(DataType data) {
        for (FilterCriterion<DataType> criteria : getCriterias()) {
            if (criteria.matches(data)) {
                return true;
            }
        }
        return false;
    }

}
