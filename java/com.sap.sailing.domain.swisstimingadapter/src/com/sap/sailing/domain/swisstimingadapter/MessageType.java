package com.sap.sailing.domain.swisstimingadapter;

public enum MessageType {
    /**
     * Available Races
     */
    RAC,
    
    /**
     * Course Configuration
     */
    CCG,
    
    /**
     * Startlist
     */
    STL,
    
    /**
     * StartTime
     */
    STT,
    
    /**
     * Clock at Mark/Finish
     */
    CAM,
    
    /**
     * Distance to Mark
     */
    DTM,
    
    /**
     * Current Boat Speed
     */
    CBS,
    
    /**
     * Distance between Boats
     */
    DBB,
    
    /**
     * Average Boat Speed per Leg
     */
    ABS,
    
    /**
     * Timing Data
     */
    TMD,
    
    /**
     * Weather Information
     */
    WEA,
    
    /**
     * Wind Data
     */
    WND,
    
    /**
     * Version
     */
    VER,
    
    /**
     * Race Position Data
     */
    RPD,
    
    /**
     * internal, inofficial event, used during testing to stop a test / dummy server instance
     */
    _STOPSERVER
}
