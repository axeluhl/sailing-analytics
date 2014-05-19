package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class RemoteSailingServerReferenceDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -4209262742778693873L;
    private String url;
    private Iterable<EventDTO> events;
    
    /**
     * The error message is usually filled in case {@link #events} is <code>null</code> and gives a hint about the
     * server-side error that occurred trying to obtain the event list from the server identified by the reference
     * represented by this DTO.
     */
    private String lastErrorMessage;

    // for GWT
    RemoteSailingServerReferenceDTO() {
    }

    public RemoteSailingServerReferenceDTO(String name, String url) {
        this(name, url, Collections. <EventDTO> emptyList());
    }
    
    public RemoteSailingServerReferenceDTO(String name, String url, Iterable<EventDTO> events) {
        this(name, url, events, /* error message */ null);
    }
    
    public RemoteSailingServerReferenceDTO(String name, String url, Iterable<EventDTO> events, String lastErrorMessage) {
        super(name);
        assert events != null;
        this.url = url;
        this.events = events;
        this.lastErrorMessage = lastErrorMessage;
    }

    public RemoteSailingServerReferenceDTO(String name, String url, String lastErrorMessage) {
        this(name, url, /* events */ null, lastErrorMessage);
    }

    public String getUrl() {
        return url;
    }

    public Iterable<EventDTO> getEvents() {
        return events;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
