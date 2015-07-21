package com.sap.sailing.gwt.server;

import java.net.URL;

import com.sap.sailing.domain.base.EventBase;

public class EventHolder {
    EventBase event;
    boolean onRemoteServer;
    URL baseURL;

    public EventHolder(EventBase event, boolean onRemoteServer, URL baseURL) {
        super();
        this.event = event;
        this.onRemoteServer = onRemoteServer;
        this.baseURL = baseURL;
    }
}