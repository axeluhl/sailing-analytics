package com.sapsailing.xrr.structureimport.eventimport;

import java.util.HashSet;
import java.util.Set;

public class EventResults {
    
    private Set<RegattaJSON> regattas = new HashSet<RegattaJSON>();
    private String id = "";
    private String name = "";
    private String xrrUrl = "";
    
    public EventResults(String id, String name, String xrrUrl){
    	this.id = id;
    	this.name = name;
    	this.xrrUrl = xrrUrl;
    }

    public Iterable<RegattaJSON> getRegattas() {
        return regattas;
    }
    
    public void addRegatta(RegattaJSON regatta){
        this.regattas.add(regatta);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getXrrUrl() {
        return xrrUrl;
    }
}
