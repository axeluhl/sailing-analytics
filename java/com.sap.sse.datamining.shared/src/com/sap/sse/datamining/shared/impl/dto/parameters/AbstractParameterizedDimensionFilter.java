package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.util.UUID;

import com.sap.sse.common.impl.RenamableImpl;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;

/**
 * Equality and hash code are based only on the {@link #getId()}, <em>not</em> on the parameter's name or value.
 */
public abstract class AbstractParameterizedDimensionFilter extends RenamableImpl implements FilterDimensionParameter {
    private static final long serialVersionUID = 3853015601496471357L;
    
    private String typeName;
    private UUID id;

    @Deprecated // GWT serialization only
    AbstractParameterizedDimensionFilter() {
        super(null);
    }
    
    public AbstractParameterizedDimensionFilter(String name, String typeName) {
        super(name);
        this.id = UUID.randomUUID();
        this.typeName = typeName;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        AbstractParameterizedDimensionFilter other = (AbstractParameterizedDimensionFilter) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
