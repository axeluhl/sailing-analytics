package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.base.Tack;

public class GPSFixDAO implements IsSerializable {
    public Date timepoint;
    public PositionDAO position;
    public SpeedWithBearingDAO speedWithBearing;
    
    /**
     * Contains one of the literals of the {@link Tack} enumeration ("STARBOARD" or "PORT")
     */
    public String tack;
    
    public GPSFixDAO() {}

    public GPSFixDAO(Date timepoint, PositionDAO position, SpeedWithBearingDAO speedWithBearing, String tack) {
        super();
        this.timepoint = timepoint;
        this.position = position;
        this.speedWithBearing = speedWithBearing;
        this.tack = tack;
    }
}
