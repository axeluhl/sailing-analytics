package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sap.sse.security.shared.dto.NamedDTO;

public class RemoteSailingServerReferenceDTO extends NamedDTO {
    private static final long serialVersionUID = -4209262742778693873L;
    private String url;
    private Iterable<EventBaseDTO> events;
    private boolean include;
    private List<UUID> selectedEvents;

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
        this(name, url, true, Collections.<UUID> emptyList(), Collections.<EventBaseDTO> emptyList());
    }

    public RemoteSailingServerReferenceDTO(String name, String url, boolean include,
            List<UUID> selectedEventIds, Iterable<EventBaseDTO> events) {
        this(name, url, include, selectedEventIds, events, /* error message */ null);
    }

    public RemoteSailingServerReferenceDTO(String name, String url, boolean include,
            List<UUID> selectedEventIds, Iterable<EventBaseDTO> events, String lastErrorMessage) {
        super(name);
        this.url = url;
        this.events = events;
        this.include = include;
        this.selectedEvents = selectedEventIds;
        this.lastErrorMessage = lastErrorMessage;
    }

    public RemoteSailingServerReferenceDTO(String name, String url, boolean include, String lastErrorMessage) {
        this(name, url, include, /* selectedEventIds */ null, /* events */ null, lastErrorMessage);
    }

    public String getUrl() {
        return url;
    }

    public Iterable<EventBaseDTO> getEvents() {
        return events;
    }
    
    public Iterable<EventBaseDTO> getInclusiveEvents() {
        return StreamSupport.stream(events.spliterator(), false)
                .filter(element -> include ? selectedEvents.contains(element.getId())
                        : !selectedEvents.contains(element.getId()))
                .collect(Collectors.toList());
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public List<UUID> getSelectedEvents() {
        return selectedEvents;
    }

    public void updateSelectedEvents(List<UUID> selectedEvents) {
        this.selectedEvents.clear();
        this.selectedEvents.addAll(selectedEvents);
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
