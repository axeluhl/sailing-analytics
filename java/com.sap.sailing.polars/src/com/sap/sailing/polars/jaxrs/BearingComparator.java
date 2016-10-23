package com.sap.sailing.polars.jaxrs;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.common.Bearing;

public class BearingComparator implements Comparator<Bearing>, Serializable {
    private static final long serialVersionUID = -3773171643340188785L;

    @Override
    public int compare(Bearing left, Bearing right) {
        return new Double(left.getDegrees()).compareTo(new Double(right.getDegrees()));
    }
}
