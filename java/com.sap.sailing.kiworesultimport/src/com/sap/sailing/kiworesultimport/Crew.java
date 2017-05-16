package com.sap.sailing.kiworesultimport;

public interface Crew {
    Skipper getSkipper();
    
    Iterable<Crewmember> getCrewmembers(); 
}
