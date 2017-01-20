package com.sap.sailing.grib;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;

public interface GribWindField {
    Wind getWind(Position position);
}
