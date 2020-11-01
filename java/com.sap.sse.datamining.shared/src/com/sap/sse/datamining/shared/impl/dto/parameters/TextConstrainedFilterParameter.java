package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public abstract class TextConstrainedFilterParameter extends AbstractParameterizedDimensionFilter {

    protected final String constraint;

    public TextConstrainedFilterParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, String constraint) {
        super(retrieverLevel, dimension);
        this.constraint = constraint.trim().toUpperCase();
    }
    
    protected String asString(Object value) {
        return value == null ? "" : value.toString().toUpperCase();
    }
    
    public String getConstraint() {
        return constraint;
    }

    @Override
    public boolean matches(Serializable value) {
        return matches(asString(value));
    }

    protected abstract boolean matches(String valueString);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((constraint == null) ? 0 : constraint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TextConstrainedFilterParameter other = (TextConstrainedFilterParameter) obj;
        if (constraint == null) {
            if (other.constraint != null)
                return false;
        } else if (!constraint.equals(other.constraint))
            return false;
        return true;
    }

}
