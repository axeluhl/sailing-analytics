package com.sap.sailing.gwt.home.client.place.event2.model;

public class RegattaDTO {
    private String name;
    private State state;
    
    public RegattaDTO() {
    }
    
    public RegattaDTO(String name, State state) {
        super();
        this.name = name;
        this.state = state;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }
}
