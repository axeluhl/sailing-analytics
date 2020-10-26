package com.sap.sailing.server.gateway.dto;

import java.util.Set;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;

public class MarkContext {
    private Mark mark;
    private Iterable<Event> events;
    private Regatta regatta;
    
    public MarkContext(Mark mark, Regatta regatta, Set<Event> events) {
        super();
        this.mark = mark;
        this.regatta = regatta;
        this.events = events;
    }
    
    public Mark getMark() {
        return mark;
    }
    public void setMark(Mark mark) {
        this.mark = mark;
    }
    public Iterable<Event> getEvent() {
        return events;
    }
    public void setEvents(Iterable<Event> events) {
        this.events = events;
    }
    public Regatta getRegatta() {
        return regatta;
    }
    public void setRegatta(Regatta regatta) {
        this.regatta = regatta;
    }
    
}
