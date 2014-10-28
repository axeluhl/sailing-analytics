package com.sap.sse.datamining.impl.criterias;

public class NonFilteringFilterCriterion<ElementType> extends AbstractFilterCriterion<ElementType> {

    public NonFilteringFilterCriterion(Class<ElementType> elementType) {
        super(elementType);
    }

    @Override
    public boolean matches(ElementType element) {
        return true;
    }

}
