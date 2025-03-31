package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;

/**
 * A wind source that in addition to its {@link WindSource#getType} has an additional identifier. This can, e.g.,
 * be used to identify the particular sensor of a type from which a set of wind fixes originated.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindSourceWithAdditionalID extends WindSourceImpl {
    private static final long serialVersionUID = -142955860536561690L;

    private final String id;

    public WindSourceWithAdditionalID(WindSourceType type, String id) {
        super(type);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        WindSourceWithAdditionalID other = (WindSourceWithAdditionalID) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}
