package com.sap.sse.datamining.impl.criterias;

import com.sap.sse.datamining.components.FilterCriterion;

public class AndCompoundFilterCriterion<ElementType> extends CompoundFilterCriterion<ElementType> {

    public AndCompoundFilterCriterion(Class<ElementType> elementType) {
        super(elementType);
    }

    @Override
    public boolean matches(ElementType data) {
        for (FilterCriterion<ElementType> criteria : getCriterias()) {
            if (!criteria.matches(data)) {
                return false;
            }
        }
        return true;
    }

}
