package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingEventRecordDTO implements IsSerializable {
    public String eventId;
    public String eventName;
    public List<SwissTimingRaceRecordDTO> races;
    public String trackingDataHost;
    public Integer trackingDataPort;
    
    SwissTimingEventRecordDTO() {}
    
    public SwissTimingEventRecordDTO(String eventId, String eventName, String trackingDataHost, Integer trackingDataPort,
            List<SwissTimingRaceRecordDTO> races) {
        super();
        this.eventId = eventId;
        this.eventName = eventName;
        this.trackingDataHost = trackingDataHost;
        this.trackingDataPort = trackingDataPort;
        this.races = races;
    }
    
}
