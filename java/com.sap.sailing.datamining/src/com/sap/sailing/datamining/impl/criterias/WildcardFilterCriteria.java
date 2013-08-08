package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.FilterCriteria;

public class WildcardFilterCriteria<DataType> implements FilterCriteria<DataType> {

    @Override
    public boolean matches(DataType data) {
        return true;
    }

}
