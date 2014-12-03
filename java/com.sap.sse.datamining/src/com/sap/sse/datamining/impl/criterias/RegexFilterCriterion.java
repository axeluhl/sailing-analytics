package com.sap.sse.datamining.impl.criterias;

public abstract class RegexFilterCriterion<ElementType> extends AbstractFilterCriterion<ElementType> {
    
    private String regex;

    public RegexFilterCriterion(Class<ElementType> elementType, String regex) {
        super(elementType);
        this.regex = regex;
    }

    @Override
    public boolean matches(ElementType data) {
        String valueToMatch = getValueToMatch(data);
        return valueToMatch != null && valueToMatch.matches(regex);
    }
    
    protected abstract String getValueToMatch(ElementType data);

}
