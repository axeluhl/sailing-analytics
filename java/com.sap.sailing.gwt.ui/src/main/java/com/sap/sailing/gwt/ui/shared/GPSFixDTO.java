package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;

public class GPSFixDTO implements IsSerializable {
    public Date timepoint;
    public PositionDTO position;
    public SpeedWithBearingDTO speedWithBearing;
    public WindDTO wind;
    
    /**
     * tells if this fix was computed by extrapolation instead of having been captured by a device directly
     * or having been interpolated between captured fixes
     */
    public boolean extrapolated;
    
    /**
     * Contains one of the literals of the {@link Tack} enumeration ("STARBOARD" or "PORT") or <code>null</code> if the tack
     * is not known
     */
    public Tack tack;
    
    /**
     * Contains one of the literals of the {@link LegType} enumeration ("UPWIND", "DOWNWIND" or "REACHING") or
     * <code>null</code> if the leg type is not known, e.g., because the competitor is not currently on any of the
     * race's legs.
     */
    public LegType legType;
    
    public GPSFixDTO() {}

    public GPSFixDTO(Date timepoint, PositionDTO position, SpeedWithBearingDTO speedWithBearing, WindDTO wind,
            Tack tack, LegType legType, boolean extrapolated) {
        super();
        this.timepoint = timepoint;
        this.position = position;
        this.speedWithBearing = speedWithBearing;
        this.tack = tack;
        this.legType = legType;
        this.extrapolated = extrapolated;
        this.wind = wind;
    }
}
