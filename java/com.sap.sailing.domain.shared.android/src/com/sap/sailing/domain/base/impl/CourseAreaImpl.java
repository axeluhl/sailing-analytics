package com.sap.sailing.domain.base.impl;

import java.util.UUID;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.Distance;
import com.sap.sse.common.impl.NamedImpl;

public class CourseAreaImpl extends NamedImpl implements CourseArea {
    private static final long serialVersionUID = 5912385360170509150L;

    private final UUID id;
    private Position centerPosition; // no setter yet; TODO bug5867; clarify replication etc.
    private Distance radius; // no setter yet; TODO bug5867; clarify replication etc.

    public CourseAreaImpl(String name, UUID id) {
        super(name);
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Position getCenterPosition() {
        return centerPosition;
    }

    @Override
    public Distance getRadius() {
        return radius;
    }

    @Override
    public CourseArea resolve(SharedDomainFactory<?> domainFactory) {
        return domainFactory.getOrCreateCourseArea(id, getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((centerPosition == null) ? 0 : centerPosition.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((radius == null) ? 0 : radius.hashCode());
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
        CourseAreaImpl other = (CourseAreaImpl) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (centerPosition == null) {
            if (other.centerPosition != null)
                return false;
        } else if (!centerPosition.equals(other.centerPosition))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (radius == null) {
            if (other.radius != null)
                return false;
        } else if (!radius.equals(other.radius))
            return false;
        return true;
    }
}
