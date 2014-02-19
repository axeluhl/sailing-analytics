package com.sap.sailing.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriteria;

public class AndCompoundFilterCriteria<DataType> extends CompoundFilterCriteria<DataType> {

    @Override
    public boolean matches(DataType data) {
        for (FilterCriteria<DataType> criteria : getCriterias()) {
            if (!criteria.matches(data)) {
                return false;
            }
        }
        return true;
    }

}
