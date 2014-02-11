package com.sap.sailing.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriteria;

public class OrCompoundFilterCriteria<DataType> extends CompoundFilterCriteria<DataType> {

    @Override
    public boolean matches(DataType data) {
        for (FilterCriteria<DataType> criteria : getCriterias()) {
            if (criteria.matches(data)) {
                return true;
            }
        }
        return false;
    }

}
