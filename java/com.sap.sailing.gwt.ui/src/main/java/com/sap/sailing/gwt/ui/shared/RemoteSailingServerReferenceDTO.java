package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.security.shared.dto.NamedDTO;

public class RemoteSailingServerReferenceDTO extends NamedDTO {
    private static final long serialVersionUID = -4209262742778693873L;
    private String url;
    private Iterable<EventBaseDTO> events;
    private List<EventBaseDTO> excludedEvents;

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
        this(name, url, Collections.<EventBaseDTO> emptyList());
    }

    public RemoteSailingServerReferenceDTO(String name, String url, Iterable<EventBaseDTO> events) {
        this(name, url, events, /* error message */ null);
    }

    public RemoteSailingServerReferenceDTO(String name, String url, Iterable<EventBaseDTO> events,
            String lastErrorMessage) {
        super(name);
        this.url = url;
        this.events = events;
        this.excludedEvents = new ArrayList<>();
        this.lastErrorMessage = lastErrorMessage;
    }

    public RemoteSailingServerReferenceDTO(String name, String url, String lastErrorMessage) {
        this(name, url, /* events */ null, lastErrorMessage);
    }

    public String getUrl() {
        return url;
    }

    public Iterable<EventBaseDTO> getEvents() {
        return events;
    }

    public List<EventBaseDTO> getExcludedEvents() {
        return excludedEvents;
    }

    public void addExcludedEvents(List<EventBaseDTO> excludedEvents) {
        this.excludedEvents.addAll(excludedEvents);
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
