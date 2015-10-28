package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.Query.QueryType;

public abstract class AbstractAdditionalQueryData implements AdditionalQueryData {

    private final QueryType type;

    public AbstractAdditionalQueryData(QueryType type) {
        this.type = type;
    }

    @Override
    public QueryType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractAdditionalQueryData other = (AbstractAdditionalQueryData) obj;
        if (type != other.type)
            return false;
        return true;
    }

}
