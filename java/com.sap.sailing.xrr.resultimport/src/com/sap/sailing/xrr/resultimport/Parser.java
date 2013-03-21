package com.sap.sailing.xrr.resultimport;


import javax.xml.bind.JAXBException;

import com.sap.sailing.xrr.resultimport.schema.Boat;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Person;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;
import com.sap.sailing.xrr.resultimport.schema.Team;

public interface Parser {
    RegattaResults parse() throws JAXBException;
    
    String getBoatClassName(Division division); 
    
    Boat getBoat(String boatID);
    
    Team getTeam(String teamID);
    
    Person getPerson(String personID);
}
