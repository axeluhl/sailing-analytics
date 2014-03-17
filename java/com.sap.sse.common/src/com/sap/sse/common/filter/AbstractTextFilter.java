package com.sap.sse.common.filter;

public abstract class AbstractTextFilter<FilterObjectType> implements TextFilter<FilterObjectType> {
    protected String value;
    protected TextOperator operator;
    
    public AbstractTextFilter() {
        value = null;
        operator = null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TextOperator getOperator() {
        return operator;
    }

    public void setOperator(FilterOperator<String> operator) {
        this.operator = (TextOperator) operator;
    }

}
