package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ManeuverDAO implements IsSerializable {
    /**
     * One of HEAD_UP, BEAR_AWAY, TACK, JIBE, PENALTY_CIRCLE, MARK_PASSING, UNKNOWN
     */
    public String type;
    
    /**
     * One of PORT or STARBOARD
     */
    public String newTack;
    
    public PositionDAO position;
    
    public Date timepoint;
    
    public SpeedWithBearingDAO speedWithBearingBefore;
    
    public SpeedWithBearingDAO speedWithBearingAfter;
    
    public double directionChangeInDegrees;

    public ManeuverDAO() {}
    
    public ManeuverDAO(String type, String newTack, PositionDAO position, Date timepoint, SpeedWithBearingDAO speedWithBearingBefore,
            SpeedWithBearingDAO speedWithBearingAfter, double directionChangeInDegrees) {
        super();
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.timepoint = timepoint;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
    }
    
}
