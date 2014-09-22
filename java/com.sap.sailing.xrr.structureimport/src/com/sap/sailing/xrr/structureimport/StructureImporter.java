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
import buildstructure.Series;
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
import eventimport.RegattaJSON;

public class StructureImporter {

    private ArrayList<RegattaResults> results = new ArrayList<RegattaResults>();
    private static List<List<List<String>>> seriesForRegattas = new ArrayList<List<List<String>>>();
    private ArrayList<RegattaJSON> regattas;
    private LinkedHashMap<String, Boat> boatForPerson;
    private DomainFactory baseDomainFactory;
    private SetRacenumberStrategy setRacenumberStrategy;
    private int parsedDocuments = 0;
    private boolean finished = false;

    public StructureImporter(SetRacenumberStrategy setRacenumber) {

        this.setRacenumberStrategy = setRacenumber;

    }

    public void parseEvent(String url) {
        regattas = new EventImport().getRegattas(url);
    }

    public List<String> getRegattaNames() {
        List<String> regattaNames = new ArrayList<String>();
        for (RegattaJSON regatta : regattas) {
            regattaNames.add(regatta.getName());
        }
        return regattaNames;
    }

    public void updateRegattasToSelected(List<String> regattaNames) {
        List<RegattaJSON> regattasTemp = new ArrayList<RegattaJSON>(regattas);
        regattas.clear();
        for (RegattaJSON regatta : regattasTemp) {
            for (String s : regattaNames) {
                if (regatta.getName().equals(s)) {
                    regattas.add(regatta);
                }
            }
        }
    }

    private void parseRegattas() {

        for (int i = 0; i < regattas.size(); i++) {
            try {
                parseRegattaXML(regattas.get(i).getXrrEntriesUrl());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            parsedDocuments++;
        }
    }
    
    public List<List<String>> getRegattaStructure(List<String> regattaNames){
        //TODO anderen Rückgabeparameter verwenden. Evtl. eine eigene DTO Klasse. 
        //Die Series aus diesem Paket kann ja nicht verwendet werden, weil es sonst wieder dependencies loops gibt  
        
        
        ArrayList<String> allSeries = new ArrayList<String>(); 
        
        results.clear();
        parseRegattas();

        for (int i = 0; i < results.size(); i++) {
            ArrayList<String> series = new ArrayList<String>();
            ArrayList<Race> races = new ArrayList<Race>();

            Event event = (Event) results.get(i).getPersonOrBoatOrTeam()
                    .get(results.get(i).getPersonOrBoatOrTeam().size() - 1);

            List<Object> raceOrDevisionOrRegattaSeries = event.getRaceOrDivisionOrRegattaSeriesResult();
            for (int j = 1; j < raceOrDevisionOrRegattaSeries.size(); j++) {
                races.add((Race) raceOrDevisionOrRegattaSeries.get(j));
            }

            BuildStructure structure = new BuildStructure(races);
            //TODO am besten in der Buildstructure im RegattaStructure eine Methode getRegattaStructure(), 
            //um die Struktur in dem gewünschten Format zu bekommen
        }
        
        return null;
    }

    public Iterable<AddSpecificRegatta> getRegattas(ScoringSchemeType scoringScheme, boolean isPersistent,
            UUID courseArea, boolean useStartTimeInference, DomainFactory baseDomainFactory,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            boolean startswithZeroScore, int[] discardingThresholds) {

        this.baseDomainFactory = baseDomainFactory;
        List<AddSpecificRegatta> regattas = new ArrayList<AddSpecificRegatta>();
        results.clear();
        parseRegattas();

        for (int i = 0; i < results.size(); i++) {

            List<Race> races = new ArrayList<Race>();

            //TODO hier die BuildStructure rausnehmen und das bestehende verwenden

            Event event = (Event) results.get(i).getPersonOrBoatOrTeam()
                    .get(results.get(i).getPersonOrBoatOrTeam().size() - 1);

            List<Object> raceOrDevisionOrRegattaSeries = event.getRaceOrDivisionOrRegattaSeriesResult();
            for (int j = 1; j < raceOrDevisionOrRegattaSeries.size(); j++) {
                races.add((Race) raceOrDevisionOrRegattaSeries.get(j));
            }

            BuildStructure structure = new BuildStructure(races);

            LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = setSeriesCreationParameters(
                    structure, firstColumnIsNonDiscardableCarryForward, hasSplitFleetContiguousScoring,
                    startswithZeroScore, discardingThresholds);

            regattas.add(new AddSpecificRegatta(RegattaImpl.getDefaultName(event.getTitle(), ((Division) event
                    .getRaceOrDivisionOrRegattaSeriesResult().get(0)).getTitle()), ((Division) event
                    .getRaceOrDivisionOrRegattaSeriesResult().get(0)).getTitle(), event.getEventID(),
                    new RegattaCreationParametersDTO(seriesCreationParams), isPersistent, this.baseDomainFactory
                            .createScoringScheme(scoringScheme), courseArea, useStartTimeInference));

        }
        return regattas;
    }

    private LinkedHashMap<String, SeriesCreationParametersDTO> setSeriesCreationParameters(BuildStructure structure,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            boolean startswithZeroScore, int[] discardingThresholds) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = new LinkedHashMap<String, SeriesCreationParametersDTO>();

        RegattaStructure regattaStructure = structure.getRegattaStructure();
        List<List<String>> series = new ArrayList<List<String>>();

        if (regattaStructure != null) {
            ArrayList<Series> raceTypes = regattaStructure.getSeries();
            for (int i = 0; i < raceTypes.size(); i++) {

                Series raceType = raceTypes.get(i);

                ArrayList<FleetDTO> fleets = getFleets(raceType.getFleets());
                List<String> raceNames = getRaceNames(i, raceType, raceType.getFleets());
                series.add(raceNames);

                seriesCreationParams.put(raceType.getSeries(), new SeriesCreationParametersDTO(fleets,
                /* medal */raceTypes.get(i).isMedal(), startswithZeroScore, firstColumnIsNonDiscardableCarryForward,
                        discardingThresholds, hasSplitFleetContiguousScoring));
            }

        }
        seriesForRegattas.add(series);
        return seriesCreationParams;

    }

    private List<String> getRaceNames(int i, Series raceType, ArrayList<Fleet> fleets) {
        List<String> raceNames = new ArrayList<String>();
        raceNames.clear();

        // set Racenumbers for each series
        Race[] races = fleets.get(raceType.getMaxIndex()).getRaces();
        for (int j = 0; j < races.length; j++) {
            Race race = races[j];
            setRacenumberStrategy.setRacenumber(race, raceType, i, raceNames);
        }
        return raceNames;
    }

    private ArrayList<FleetDTO> getFleets(ArrayList<Fleet> fleets) {
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
        return fleetsDTO;
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

            boatClass = getBoatClass(regattas.get(i));

            ArrayList<Object> personOrBoatOrTeam = (ArrayList<Object>) results.get(i).getPersonOrBoatOrTeam();

            setBoatsAndTeamsForPerson(personOrBoatOrTeam);

            for (Object obj : personOrBoatOrTeam) {
                if (obj instanceof Person) {
                    Person person = (Person) obj;
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

    private BoatClass getBoatClass(RegattaJSON regatta) {
        BoatClass boatClass;
        if (regatta.getBoatClass() != null) {
            boatClass = baseDomainFactory.getOrCreateBoatClass(regatta.getBoatClass());
        } else {
            boatClass = baseDomainFactory.getOrCreateBoatClass("default");
        }
        return boatClass;
    }

    private Nationality getNationality(String country) {
        return baseDomainFactory.getOrCreateNationality(country);
    }

    private void setBoatsAndTeamsForPerson(ArrayList<Object> personOrBoatOrTeam) {

        boatForPerson = new LinkedHashMap<String, Boat>();
        LinkedHashMap<String, Team> teamForBoat = new LinkedHashMap<String, Team>();

        for (Object obj : personOrBoatOrTeam) {
            if (obj instanceof Team) {
                teamForBoat.put(((Team) obj).getBoatID(), (Team) obj);
            }
        }

        for (Object obj : personOrBoatOrTeam) {
            if (obj instanceof Boat) {
                Team team = teamForBoat.get(((Boat) obj).getBoatID());

                ArrayList<Crew> crew = (ArrayList<Crew>) team.getCrew();
                for (Crew person : crew) {
                    boatForPerson.put(person.getPersonID(), (Boat) obj);
                }
            }
        }

    }

    private BoatAndTeam getBoatAndTeam(String idAsString, String name, Nationality nationality, BoatClass boatClass) {

        DynamicBoat boat = createBoat(name, boatForPerson.get(idAsString), boatClass);
        if (nationality == null) {
            nationality = createNationalityFromSailID(boat.getSailID());
        }
        DynamicTeam team = createTeam(name, nationality);
        return new BoatAndTeam(boat, team);
    }

    private Nationality createNationalityFromSailID(String sailID) {
        String country = "";
        if (sailID.length() >= 3) {
            for (int i = 0; i < 3; i++) {
                country += sailID.charAt(i);
            }

            return getNationality(country);
        }
        return null;
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
