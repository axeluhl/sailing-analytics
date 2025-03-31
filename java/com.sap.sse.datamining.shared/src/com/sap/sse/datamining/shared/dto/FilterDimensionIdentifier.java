package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

/**
 * Within a {@link StatisticQueryDefinitionDTO} the {@link StatisticQueryDefinitionDTO#getFilterSelection() filter
 * selections} are keyed by a combination of {@link DataRetrieverLevelDTO} and dimension {@link FunctionDTO function}.<p>
 * 
 * {@link Object#equals(Object)} and {@link Object#hashCode()} are defined based on the {@link #retrieverLevel} and
 * {@link #dimensionFunction} equality/hashCode definitions.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FilterDimensionIdentifier implements Serializable {
    private static final long serialVersionUID = -4824907338023614296L;
    private DataRetrieverLevelDTO retrieverLevel;
    private FunctionDTO dimensionFunction;

    @Deprecated // for GWT serialization only
    FilterDimensionIdentifier() {}
    
    public FilterDimensionIdentifier(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimensionFunction) {
        super();
        this.retrieverLevel = retrieverLevel;
        this.dimensionFunction = dimensionFunction;
    }

    public DataRetrieverLevelDTO getRetrieverLevel() {
        return retrieverLevel;
    }

    public FunctionDTO getDimensionFunction() {
        return dimensionFunction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dimensionFunction == null) ? 0 : dimensionFunction.hashCode());
        result = prime * result + ((retrieverLevel == null) ? 0 : retrieverLevel.hashCode());
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
        FilterDimensionIdentifier other = (FilterDimensionIdentifier) obj;
        if (dimensionFunction == null) {
            if (other.dimensionFunction != null)
                return false;
        } else if (!dimensionFunction.equals(other.dimensionFunction))
            return false;
        if (retrieverLevel == null) {
            if (other.retrieverLevel != null)
                return false;
        } else if (!retrieverLevel.equals(other.retrieverLevel))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "" + retrieverLevel + "/" + dimensionFunction;
    }
}
