package com.sap.sailing.polars.mining;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sse.common.Bearing;

public class BearingComparator implements Comparator<Bearing>, Serializable {

    private static final long serialVersionUID = 8166601046140275541L;

    @Override
    public int compare(Bearing arg0, Bearing arg1) {
        return new Double(arg0.getDegrees()).compareTo(new Double(arg1.getDegrees()));
    }
    
}