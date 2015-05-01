package com.sap.sse.datamining.impl;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sse.datamining.DataMiningQueryManager;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.SingleQueryPerDimensionManager.Key;
import com.sap.sse.datamining.shared.DataMiningSession;

public class SingleQueryPerDimensionManager extends SingleQueryPerKeyManager<Key> {

    @Override
    protected <ResultType> Iterable<Key> getKeysFor(DataMiningSession session, Query<ResultType> query) {
        AdditionalDimensionValuesQueryData additionalData = query.getAdditionalData(AdditionalDimensionValuesQueryData.class);
        if (additionalData == null) {
            throw new IllegalArgumentException("This " + DataMiningQueryManager.class.getSimpleName()
                                               + " can only manage queries of the " + QueryType.class.getSimpleName()
                                               + " " + QueryType.DIMENSION_VALUES);
        }
        
        Collection<Key> keys = new HashSet<>();
        for (Function<?> dimension : additionalData.getDimensions()) {
            keys.add(new Key(session, dimension));
        }
        return keys;
    }
    
    static class Key {
        
        private DataMiningSession session;
        private Function<?> dimension;
        
        public Key(DataMiningSession session, Function<?> dimension) {
            this.session = session;
            this.dimension = dimension;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dimension == null) ? 0 : dimension.hashCode());
            result = prime * result + ((session == null) ? 0 : session.hashCode());
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
            Key other = (Key) obj;
            if (dimension == null) {
                if (other.dimension != null)
                    return false;
            } else if (!dimension.equals(other.dimension))
                return false;
            if (session == null) {
                if (other.session != null)
                    return false;
            } else if (!session.equals(other.session))
                return false;
            return true;
        }
        
    }

}
