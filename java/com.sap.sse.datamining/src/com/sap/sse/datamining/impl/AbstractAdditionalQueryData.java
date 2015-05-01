package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.Query.QueryType;

public abstract class AbstractAdditionalQueryData implements AdditionalQueryData {

    private final QueryType type;
    private final UUID dataRetrieverChainID;

    public AbstractAdditionalQueryData(QueryType type, UUID dataRetrieverChainID) {
        this.type = type;
        this.dataRetrieverChainID = dataRetrieverChainID;
    }

    @Override
    public QueryType getType() {
        return type;
    }

    @Override
    public UUID getDataRetrieverChainID() {
        return dataRetrieverChainID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataRetrieverChainID == null) ? 0 : dataRetrieverChainID.hashCode());
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
        if (dataRetrieverChainID == null) {
            if (other.dataRetrieverChainID != null)
                return false;
        } else if (!dataRetrieverChainID.equals(other.dataRetrieverChainID))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
