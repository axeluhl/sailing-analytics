package com.sap.sailing.domain.swisstimingadapter;

public enum MessageType {
    /**
     * Open the connection to race with specific raceID.
     * 
     * <pre>
     * Client --&gt; LiveTrakingServer
     * &lt;STX&gt;OPN|RaceId&lt;ETX&gt;
     * LiveTrackingServer --&gt; Client
     * &lt;STX&gt;OPN!|{OK:FAILED}[|Last Message Number]&lt;ETX&gt;
     * Remark: Last Message Number will be sent only if result is “OK”
     * </pre>
     */
    OPN(/* isRaceSpecific */ true),
    
    /**
     * Listen for the live messages of a race.
     * 
     * <ul>
     * <li> By default, sending of live messages is not activated and can be started via this function </li>
     * <li> Message number parameter indicates the message, from which the live stream should begin </li>
     * <li> If the Message number parameter is missing, all messages will be send to the client </li>
     * <li> Race Messages will be send asynchronously after the answer to this command has been send </li>
     * </ul>
     * <pre>
     * Client --&gt; LiveTrackingServer
     * &lt;STX&gt;LSN|{ON:OFF}[|Message number]&lt;ETX&gt;
     * LiveTrackingServer --&gt; Client
     * &lt;STX&gt;LSN!|{OK:FAILED}&lt;ETX&gt;
     * </pre>
     */
    LSN(/* isRaceSpecific */ true),
    
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
