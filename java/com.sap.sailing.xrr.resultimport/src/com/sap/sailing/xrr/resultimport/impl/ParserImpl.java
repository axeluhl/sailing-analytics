package com.sap.sailing.xrr.resultimport.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.schema.Boat;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Person;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;
import com.sap.sailing.xrr.resultimport.schema.Team;

public class ParserImpl implements Parser {
    private final Map<String, Person> personByID;
    private final Map<String, Team> teamByID;
    private final Map<String, Boat> boatByID;
    private final InputStream inputStream;
    
    public ParserImpl(InputStream inputStream) {
        super();
        this.personByID = new HashMap<>();
        this.teamByID = new HashMap<>();
        this.boatByID = new HashMap<>();
        this.inputStream = inputStream;
    }

    @Override
    public RegattaResults parse() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("com.sap.sailing.xrr.resultimport.schema", ParserImpl.class.getClassLoader());
        Unmarshaller um = jc.createUnmarshaller();
        @SuppressWarnings("unchecked")
        RegattaResults regattaResults = ((JAXBElement<RegattaResults>) um.unmarshal(inputStream)).getValue();
        for (Object o : regattaResults.getPersonOrBoatOrTeam()) {
            if (o instanceof Person) {
                Person person = (Person) o;
                personByID.put(person.getPersonID(), person);
            } else if (o instanceof Boat) {
                Boat boat = (Boat) o;
                boatByID.put(boat.getBoatID(), boat);
            } else if (o instanceof Team) {
                Team team = (Team) o;
                teamByID.put(team.getTeamID(), team);
            }
        }
        return regattaResults;
    }

    @Override
    public String getBoatClassName(Division division) {
        String result = division.getIFClassID();
        if (result == null) {
            result = division.getTitle();
        }
        return result;
    }

    @Override
    public Boat getBoat(String boatID) {
        return boatByID.get(boatID);
    }

    @Override
    public Team getTeam(String teamID) {
        return teamByID.get(teamID);
    }

    @Override
    public Person getPerson(String personID) {
        return personByID.get(personID);
    }
}
