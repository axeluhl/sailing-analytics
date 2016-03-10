package com.sap.sailing.yachtscoring;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the list of regattas for an event with a given yachtscoring event id
 * @author Frank
 *
 */
public class EventResultDescriptor {
    private List<RegattaResultDescriptor> regattaResults;
    private String id;
    private String isafId;
    private String name;
    private URL xrrUrl;

    private String trackingDataHost;
    private Integer trackingDataPort;
    
    public EventResultDescriptor() {
        regattaResults = new ArrayList<RegattaResultDescriptor>();
    }

    public List<RegattaResultDescriptor> getRegattaResults() {
        return regattaResults;
    }

    public void setRegattaResults(List<RegattaResultDescriptor> regattaResults) {
        this.regattaResults = regattaResults;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URL getXrrUrl() {
        return xrrUrl;
    }

    public void setXrrUrl(URL xrrUrl) {
        this.xrrUrl = xrrUrl;
    }

    public String getIsafId() {
        return isafId;
    }

    public void setIsafId(String isafId) {
        this.isafId = isafId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrackingDataHost() {
        return trackingDataHost;
    }

    public void setTrackingDataHost(String trackingDataHost) {
        this.trackingDataHost = trackingDataHost;
    }

    public Integer getTrackingDataPort() {
        return trackingDataPort;
    }

    public void setTrackingDataPort(Integer trackingDataPort) {
        this.trackingDataPort = trackingDataPort;
    }
}
