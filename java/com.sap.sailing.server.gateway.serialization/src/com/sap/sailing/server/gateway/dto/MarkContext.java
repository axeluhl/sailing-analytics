package com.sap.sailing.server.gateway.dto;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;

public class MarkContext {
    private Mark mark;
    private Event event;
    private Regatta regatta;
    
    public MarkContext(Mark mark, Event event, Regatta regatta) {
        super();
        this.mark = mark;
        this.event = event;
        this.regatta = regatta;
    }
    
    public Mark getMark() {
        return mark;
    }
    public void setMark(Mark mark) {
        this.mark = mark;
    }
    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
    public Regatta getRegatta() {
        return regatta;
    }
    public void setRegatta(Regatta regatta) {
        this.regatta = regatta;
    }
    
}
