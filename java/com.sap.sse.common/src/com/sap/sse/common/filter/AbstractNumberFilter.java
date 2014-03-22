package com.sap.sse.common.filter;

public abstract class AbstractNumberFilter<FilterObjectType, T extends Number> implements NumberFilter<FilterObjectType, T> {
    protected T value;
    protected FilterOperator<T> operator;
    
    public AbstractNumberFilter() {
        value = null;
        operator = null;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public FilterOperator<T> getOperator() {
        return operator;
    }

    public void setOperator(FilterOperator<T> operator) {
        this.operator = operator;
    }
}
