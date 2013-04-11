package com.sap.sailing.odf.resultimport;

public interface Crew {
    Skipper getSkipper();
    
    Iterable<Crewmember> getCrewmembers(); 
}
