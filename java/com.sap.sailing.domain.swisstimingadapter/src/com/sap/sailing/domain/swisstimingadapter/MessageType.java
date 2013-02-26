package com.sap.sailing.domain.swisstimingadapter;

public enum MessageType {
    /**
     * Available Races
     */
    RAC(/* isRaceSpecific */ false),
    
    /**
     * Course Configuration
     */
    CCG(/* isRaceSpecific */ true),
    
    /**
     * Startlist
     */
    STL(/* isRaceSpecific */ true),
    
    /**
     * StartTime
     */
    STT(/* isRaceSpecific */ true),
    
    /**
     * Clock at Mark/Finish
     */
    CAM(/* isRaceSpecific */ true),
    
    /**
     * Distance to Mark
     */
    DTM(/* isRaceSpecific */ true),
    
    /**
     * Current Boat Speed
     */
    CBS(/* isRaceSpecific */ true),
    
    /**
     * Distance between Boats
     */
    DBB(/* isRaceSpecific */ true),
    
    /**
     * Average Boat Speed per Leg
     */
    ABS(/* isRaceSpecific */ true),
    
    /**
     * Timing Data
     */
    TMD(/* isRaceSpecific */ true),
    
    /**
     * Weather Information
     */
    WEA(/* isRaceSpecific */ true),
    
    /**
     * Wind Data
     */
    WND(/* isRaceSpecific */ true),
    
    /**
     * Version
     */
    VER(/* isRaceSpecific */ false),
    
    /**
     * Race Position Data
     */
    RPD(/* isRaceSpecific */ true),
    
    /**
     * internal, inofficial event, used during testing to stop a test / dummy server instance
     */
    _STOPSERVER(/* isRaceSpecific */ false);
    
    private boolean isRaceSpecific;
    
    private MessageType(boolean isRaceSpecific) {
        this.isRaceSpecific = isRaceSpecific;
    }
    
    public boolean isRaceSpecific() {
        return isRaceSpecific;
    }
}
