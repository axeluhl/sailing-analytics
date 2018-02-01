package com.sap.sailing.gwt.ui.shared;

public class EventAndRegattaDTO {
    private EventDTO event;
    private RegattaDTO regatta;

    public EventAndRegattaDTO(EventDTO event, RegattaDTO regatta) {
        super();
        this.event = event;
        this.regatta = regatta;
    }

    public EventDTO getEvent() {
        return event;
    }

    public void setEvent(EventDTO event) {
        this.event = event;
    }

    public RegattaDTO getRegatta() {
        return regatta;
    }

    public void setRegatta(RegattaDTO regatta) {
        this.regatta = regatta;
    }
}
