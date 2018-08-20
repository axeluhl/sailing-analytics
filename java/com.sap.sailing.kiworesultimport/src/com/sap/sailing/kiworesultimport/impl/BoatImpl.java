package com.sap.sailing.kiworesultimport.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.BoatResultInRace;
import com.sap.sailing.kiworesultimport.Crew;

public class BoatImpl extends NamedImpl implements Boat {

    public BoatImpl(Node node) {
        super(node);
    }

    @Override
    public String getSailingNumber() {
        return getNode().getAttributes().getNamedItem("sailingnumber").getNodeValue();
    }

    @Override
    public Integer getRank() {
        return Integer.valueOf(getNode().getAttributes().getNamedItem("position").getNodeValue());
    }

    @Override
    public String getPrice() {
        return getNode().getAttributes().getNamedItem("preis").getNodeValue();
    }

    @Override
    public Crew getCrew() {
        final NodeList crew = ((Element) getNode()).getElementsByTagName("Crew");
        return new CrewImpl(crew.item(0));
    }
    
    @Override
    public Double getTotalPoints() {
        return Double.valueOf(((Element) getNode()).getElementsByTagName("Races").item(0).getAttributes()
                .getNamedItem("totalpoints").getNodeValue().replace(',', '.'));
    }

    @Override
    public Iterable<BoatResultInRace> getResultsInRaces() {
        List<BoatResultInRace> result = new ArrayList<BoatResultInRace>();
        final NodeList races = ((Element) ((Element) getNode()).getElementsByTagName("Races").item(0)).getElementsByTagName("Race");
        for (int i=0; i<races.getLength(); i++) {
            result.add(new BoatResultsInRaceImpl(races.item(i)));
        }
        return result;
    }

    @Override
    public BoatResultInRace getResultsInRace(int raceNumberOneBased) {
        for (BoatResultInRace race : getResultsInRaces()) {
            if (race.getRaceNumber() == raceNumberOneBased) {
                return race;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getSailingNumber();
    }
}
