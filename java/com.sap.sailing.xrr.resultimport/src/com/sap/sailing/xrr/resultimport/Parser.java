package com.sap.sailing.xrr.resultimport;

import javax.xml.bind.JAXBException;

import com.sap.sailing.xrr.schema.Boat;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.Person;
import com.sap.sailing.xrr.schema.Race;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sailing.xrr.schema.Team;

public interface Parser {
    RegattaResults parse() throws JAXBException;
    
    String getBoatClassName(Division division); 
    
    Boat getBoat(String boatID);
    
    Team getTeam(String teamID);
    
    Person getPerson(String personID);

    Race getRace(String raceID);
}
