package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public class AndCompoundFilterCriteria<DataType> extends CompoundFilterCriteria<DataType> {

    @Override
    public boolean matches(DataType data) {
        for (ConcurrentFilterCriteria<DataType> criteria : getCriterias()) {
            if (!criteria.matches(data)) {
                return false;
            }
        }
        return true;
    }

}
