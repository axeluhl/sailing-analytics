package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriterion;

public class OrCompoundFilterCriterion<ElementType> extends CompoundFilterCriterion<ElementType> {

    public OrCompoundFilterCriterion(Class<ElementType> elementType) {
        super(elementType);
    }

    @Override
    public boolean matches(ElementType data) {
        for (FilterCriterion<ElementType> criteria : getCriterias()) {
            if (criteria.matches(data)) {
                return true;
            }
        }
        return false;
    }

}
