package com.sap.sailing.xrr.structureimport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import buildstructure.BuildStructure;
import buildstructure.Fleet;
import buildstructure.SetRacenumberStrategy;
import buildstructure.RaceType;
import buildstructure.RegattaStructure;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.schema.Boat;
import com.sap.sailing.xrr.schema.Crew;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.Event;
import com.sap.sailing.xrr.schema.Person;
import com.sap.sailing.xrr.schema.Race;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sailing.xrr.schema.Team;

import eventimport.EventImport;
import eventimport.Regattas;

public class StructureImportUrl {

    private ArrayList<RegattaResults> results = new ArrayList<RegattaResults>();
    private String eventName = "";
    private static List<List<List<String>>> seriesForRegattas = new ArrayList<List<List<String>>>();
    private ArrayList<Regattas> regattas;
    private LinkedHashMap<String, Boat> boatForPerson;
    private DomainFactory baseDomainFactory;
    private SetRacenumberStrategy setRacenumberStrategy;
    private int parsedDocuments = 0;
    private boolean finished = false;

    public StructureImportUrl(String url, SetRacenumberStrategy setRacenumber) {

        this.setRacenumberStrategy = setRacenumber;
        regattas = new EventImport().getRegattas(url);

    }

    public ArrayList<Regattas> getRegattas() {
        return regattas;
    }

    public void updateRegattasToSelected(List<String> regattaNames) {
        ArrayList<Regattas> regattasTemp = new ArrayList<Regattas>();
        for (Regattas regatta : regattas) {
            for (String s : regattaNames) {
                if (regatta.getName().equals(s)) {
                    regattasTemp.add(regatta);
                }
            }
        }
        regattas = regattasTemp;
    }

    public void parseEvent() {

        for (int i = 0; i < regattas.size(); i++) {
            try {
                parseRegattaXML(regattas.get(i).getXrrEntriesUrl());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JAXBException e) {
                // TODO Auto-generated catch blocks
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            parsedDocuments++;
        }

    }

    public ArrayList<AddSpecificRegatta> getRegattas(ScoringSchemeType scoringScheme, boolean isPersistent,
            UUID courseArea, boolean useStartTimeInference, DomainFactory baseDomainFactory,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            boolean startswithZeroScore, int[] discardingThresholds) {

        this.baseDomainFactory = baseDomainFactory;
        ArrayList<AddSpecificRegatta> regattas = new ArrayList<AddSpecificRegatta>();
        Event event = null;
        results.clear();
        parseEvent();

        for (int i = 0; i < results.size(); i++) {

            ArrayList<Race> races = new ArrayList<Race>();

            event = (Event) results.get(i).getPersonOrBoatOrTeam()
                    .get(results.get(i).getPersonOrBoatOrTeam().size() - 1);

            List<Object> raceOrDevisionOrRegattaSeries = event.getRaceOrDivisionOrRegattaSeriesResult();
            for (int j = 1; j < raceOrDevisionOrRegattaSeries.size(); j++) {
                races.add((Race) raceOrDevisionOrRegattaSeries.get(j));
            }

            BuildStructure structure = new BuildStructure(races);

            LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = setSeriesCreationParametersDTO(
                    structure, firstColumnIsNonDiscardableCarryForward, hasSplitFleetContiguousScoring,
                    startswithZeroScore, discardingThresholds);

            regattas.add(new AddSpecificRegatta(RegattaImpl.getDefaultName(event.getTitle(), ((Division) event
                    .getRaceOrDivisionOrRegattaSeriesResult().get(0)).getTitle()), ((Division) event
                    .getRaceOrDivisionOrRegattaSeriesResult().get(0)).getTitle(), event.getEventID(),
                    new RegattaCreationParametersDTO(seriesCreationParams), isPersistent, this.baseDomainFactory
                            .createScoringScheme(scoringScheme), courseArea, useStartTimeInference));

        }
        eventName = event.getTitle();
        return regattas;
    }

    public String getEventName() {
        return eventName;
    }

    private LinkedHashMap<String, SeriesCreationParametersDTO> setSeriesCreationParametersDTO(BuildStructure structure,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            boolean startswithZeroScore, int[] discardingThresholds) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = new LinkedHashMap<String, SeriesCreationParametersDTO>();

        RegattaStructure regattaStructure = structure.getRegattaStructure();
        List<List<String>> series = new ArrayList<List<String>>();

        if (regattaStructure != null) {
            ArrayList<RaceType> raceTypes = regattaStructure.getRaceTypes();
            for (int i = 0; i < raceTypes.size(); i++) {

                RaceType raceType = raceTypes.get(i);

                ArrayList<Fleet> fleets = raceType.getFleets();
                ArrayList<FleetDTO> fleetsDTO = new ArrayList<FleetDTO>();
                String fleetColor;
                for (int j = 0; j < fleets.size(); j++) {
                    if (fleets.size() <= 1) {
                        fleetColor = "Default";
                    } else {
                        fleetColor = fleets.get(j).getColor();
                    }

                    FleetDTO fleetDTO = new FleetDTO(fleetColor, j, getColorFromString(fleetColor));
                    fleetsDTO.add(fleetDTO);
                }
                List<String> raceNames = new ArrayList<String>();
                raceNames.clear();

                // set Racenumbers for each series
                Race[] races = fleets.get(raceType.getMaxIndex()).getRaces();
                for (int j = 0; j < races.length; j++) {
                    Race race = races[j];
                    setRacenumberStrategy.setRacenumber(race, raceType, i, raceNames);
                }
                series.add(raceNames);

                seriesCreationParams.put(raceType.getRaceType(), new SeriesCreationParametersDTO(fleetsDTO,
                /* medal */raceTypes.get(i).isMedal(), startswithZeroScore, firstColumnIsNonDiscardableCarryForward,
                        discardingThresholds, hasSplitFleetContiguousScoring));
            }

        }
        seriesForRegattas.add(series);
        return seriesCreationParams;

    }

    private Color getColorFromString(String colorString) {
        Color color = null;

        switch (colorString) {
        case "Blue":
            color = Color.BLUE;
            break;
        case "Red":
            color = Color.RED;
            break;
        case "Yellow":
            color = Color.YELLOW;
            break;
        case "Green":
            color = Color.GREEN;
            break;
        case "Gold":
            color = Color.ORANGE;
            break;
        case "Silver":
            color = Color.LIGHT_GRAY;
            break;
        case "Bronze":
            color = Color.ORANGE;
            break;
        case "Emerald":
            color = Color.CYAN;
            break;
        default:
            color = Color.BLACK;
        }

        return color;
    }

    public void setCompetitors() {

        for (int i = 0; i < results.size(); i++) {
            BoatClass boatClass = null;

            if (regattas.get(i).getBoatClass() != null) {
                boatClass = baseDomainFactory.getOrCreateBoatClass(regattas.get(i).getBoatClass());
            } else {
                boatClass = baseDomainFactory.getOrCreateBoatClass("default");
            }

            ArrayList<Object> personOrBoatOrTeam = (ArrayList<Object>) results.get(i).getPersonOrBoatOrTeam();

            setBoatsAndTeamsForPerson(personOrBoatOrTeam);

            for (int j = 0; j < personOrBoatOrTeam.size(); j++) {
                if (personOrBoatOrTeam.get(j) instanceof Person) {
                    Person person = (Person) personOrBoatOrTeam.get(j);
                    String idAsString = person.getPersonID();

                    String name = person.getGivenName() + " " + person.getFamilyName();
                    Color color = null;
                    Nationality nationality = (person.getNOC() == null) ? null : getNationality(person.getNOC()
                            .toString());

                    BoatAndTeam boatAndTeam = getBoatAndTeam(idAsString, name, nationality, boatClass);

                    baseDomainFactory.convertToCompetitorDTO(baseDomainFactory.getOrCreateCompetitor(
                            UUID.fromString(idAsString), name, color, boatAndTeam.getTeam(), boatAndTeam.getBoat()));
                } else {
                    break;
                }
            }
        }

    }

    private Nationality getNationality(String country) {
        return baseDomainFactory.getOrCreateNationality(country);
    }

    private void setBoatsAndTeamsForPerson(ArrayList<Object> personOrBoatOrTeam) {

        boatForPerson = new LinkedHashMap<String, Boat>();
        LinkedHashMap<String, Team> teamForBoat = new LinkedHashMap<String, Team>();

        for (int i = 0; i < personOrBoatOrTeam.size(); i++) {
            if (personOrBoatOrTeam.get(i) instanceof Team) {
                teamForBoat.put(((Team) personOrBoatOrTeam.get(i)).getBoatID(), (Team) personOrBoatOrTeam.get(i));
            }
        }

        for (int i = 0; i < personOrBoatOrTeam.size(); i++) {
            if (personOrBoatOrTeam.get(i) instanceof Boat) {
                Team team = teamForBoat.get(((Boat) personOrBoatOrTeam.get(i)).getBoatID());

                ArrayList<Crew> crew = (ArrayList<Crew>) team.getCrew();
                for (int j = 0; j < crew.size(); j++) {
                    boatForPerson.put(crew.get(j).getPersonID(), (Boat) personOrBoatOrTeam.get(i));
                }
            }
        }

    }

    private BoatAndTeam getBoatAndTeam(String idAsString, String name, Nationality nationality, BoatClass boatClass) {

        DynamicBoat boat = createBoat(name, boatForPerson.get(idAsString), boatClass);
        if (nationality == null) {
            String country = "";
            if (boat.getSailID().length() >= 3) {
                for (int i = 0; i < 3; i++) {
                    country += boat.getSailID().charAt(i);
                }

                nationality = getNationality(country);
            }
        }
        DynamicTeam team = createTeam(name, nationality);
        return new BoatAndTeam(boat, team);
    }

    private DynamicBoat createBoat(String name, Boat boat, BoatClass boatClass) {

        DynamicBoat boat1 = new BoatImpl(name + " boat", boatClass, boat.getSailNumber());
        return boat1;
    }

    private DynamicTeam createTeam(String name, Nationality nationality) {
        DynamicPerson sailor = new PersonImpl(name, nationality, null, null);
        DynamicTeam team = new TeamImpl(name + " team", Collections.singleton(sailor), null);
        return team;
    }

    public List<List<List<String>>> getRaceNames() {
        return seriesForRegattas;
    }

    private void parseRegattaXML(String url) throws FileNotFoundException, JAXBException, IOException {
        results.add(ParserFactory.INSTANCE.createParser(getInputStream(url), "").parse());

    }

    private InputStream getInputStream(String url) throws FileNotFoundException, IOException {

        URLConnection connection = new URL(url).openConnection();

        return connection.getInputStream();
    }

    public void resetSeriesForRegattas() {
        seriesForRegattas.clear();
    }

    public int getProgress() {
        return parsedDocuments;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

}
