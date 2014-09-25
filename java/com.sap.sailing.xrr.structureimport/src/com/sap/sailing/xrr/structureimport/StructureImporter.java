package com.sap.sailing.xrr.structureimport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import buildstructure.BuildStructure;
import buildstructure.Fleet;
import buildstructure.RegattaStructure;
import buildstructure.Series;
import buildstructure.SetRacenumberStrategy;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.FleetColors;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.impl.ScoreCorrectionProviderImpl;
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
	
	private static final Logger logger = Logger.getLogger(StructureImporter.class.getName());
    private final Set<RegattaResults> results = Collections.synchronizedSet(new HashSet<RegattaResults>());
    private Map<RegattaStructureKey, Set<BuildStructure>> seriesStructuresWithFrequency = new HashMap<RegattaStructureKey, Set<BuildStructure>>(); // TODO
                                                                                                                                                         // rename
    private List<BuildStructure> buildStructures = new ArrayList<BuildStructure>();
    private LinkedHashMap<String, Boat> boatForPerson;
    private DomainFactory baseDomainFactory;
    private final SetRacenumberStrategy setRacenumberStrategy;
    private int parsedDocuments = 0;
    private boolean finished = false;

    public StructureImporter(SetRacenumberStrategy setRacenumber) {
        this.setRacenumberStrategy = setRacenumber;
    }
    
    public StructureImporter() {
        this(null);
    }

    public Iterable<RegattaJSON> parseEvent(String url) {
        return new EventImport().getRegattas(url);
    }

    public Iterable<String> getRegattaNames(Iterable <RegattaJSON> regattas, DomainFactory baseDomainFactory) {
        parseRegattas(regattas);
        for (RegattaResults result : results) {
            List<Race> races = new ArrayList<Race>();
            Event event = (Event) result.getPersonOrBoatOrTeam().get(result.getPersonOrBoatOrTeam().size() - 1);
            Iterable<Object> raceOrDevisionOrRegattaSeriesResults = event.getRaceOrDivisionOrRegattaSeriesResult();
            
            LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = setSeriesCreationParameters(
                    buildStructures.get(i), false, false,
                    false, null);
            this.baseDomainFactory = baseDomainFactory;
            Set<Regatta> addSpecificRegattas = new HashSet<Regatta>();
            addSpecificRegattas.add((Regatta)new AddSpecificRegatta(RegattaImpl.getDefaultName(event.getTitle(),
                    ((Division) event.getRaceOrDivisionOrRegattaSeriesResult().get(0)).getTitle()), ((Division) event
                    .getRaceOrDivisionOrRegattaSeriesResult().get(0)).getTitle(), event.getEventID(),
                    new RegattaCreationParametersDTO(seriesCreationParams), false, this.baseDomainFactory
                            .createScoringScheme(ScoringSchemeType.LOW_POINT), null /*courseAreaUUID*/, true));
            
            for (Object raceOrDevisionOrRegattaSeriesResult: raceOrDevisionOrRegattaSeriesResults) {
                races.add((Race) raceOrDevisionOrRegattaSeriesResult);
            }

            BuildStructure structure = new BuildStructure(races);
//            analyseStructure(structure);
            buildStructures.add(structure);
        }
        return seriesStructuresWithFrequency;
    }
    
    public Iterable<Regatta> getRegattas(DomainFactory baseDomainFactory) {

        this.baseDomainFactory = baseDomainFactory;
        Set<Regatta> addSpecificRegattas = new HashSet<Regatta>();

        for (int i = 0; i < results.size(); i++) {


        }
        return addSpecificRegattas;
    }

    private Iterable<RegattaResults> parseRegattas(final Iterable<RegattaJSON> selectedRegattas) {
    	Set<Thread> threads = new HashSet<Thread>();
        for (final RegattaJSON selectedRegatta : selectedRegattas) {
        	Thread thread = new Thread("XRR Importer "+selectedRegatta.getName()) {
                @Override
                public void run() {
                    try {
                        parseRegattaXML(selectedRegatta.getXrrEntriesUrl());
                    } catch (JAXBException | IOException e) {
                    	logger.info("Parse error during XRR import. Ignoring document "+selectedRegatta.getName());
                    }
                    parsedDocuments++;
                }
            };
            thread.start();
            threads.add(thread);
        }
        while (true) {
            try {
                for (Thread thread : threads) {
					thread.join();
				}
                break;
            }
            catch (InterruptedException e) {
            	//not all Regattas have been parsed
            }
        }
        return results;
    }

    public Map<RegattaStructureKey, Set<BuildStructure>> getRegattaStructures(List<String> regattaNames) {
        buildStructures.clear();
        results.clear();
        int zaehler = 0;

        for (RegattaResults result : results) {
            List<Race> races = new ArrayList<Race>();

            Event event = (Event) result.getPersonOrBoatOrTeam().get(result.getPersonOrBoatOrTeam().size() - 1);

            List<Object> raceOrDevisionOrRegattaSeries = event.getRaceOrDivisionOrRegattaSeriesResult();
            for (int j = 1; j < raceOrDevisionOrRegattaSeries.size(); j++) {
                races.add((Race) raceOrDevisionOrRegattaSeries.get(j));
            }

            BuildStructure structure = new BuildStructure(races);
//            analyseStructure(structure);
            buildStructures.add(structure);
            zaehler++;
        }

        return seriesStructuresWithFrequency;
    }

//    public void analyseStructure(BuildStructure structure) {
//        Map<String,String> seriesAndFleets = new HashMap<String,String>();
//        for (Series series : structure.getRegattaStructure().getSeries()) {
//            seriesAndFleets.add(series.getSeries());
//            for (Fleet fleet : series.getFleets()) {
//                seriesAndFleets.add(fleet.getColor());
//            }
//        }
//        RegattaStructureKey regattaStructureKey = new RegattaStructureKey(seriesAndFleets);
//
//        if (!seriesAndFleets.isEmpty()) {
//            if (seriesStructuresWithFrequency.containsKey(regattaStructureKey)) {
//                seriesStructuresWithFrequency.get(regattaStructureKey).add(structure);
//            } else {
//                Set<BuildStructure> buildStructures = new HashSet<BuildStructure>();
//                buildStructures.add(structure);
//                seriesStructuresWithFrequency.put(regattaStructureKey, buildStructures);
//            }
//        }
//
//    }

    private LinkedHashMap<String, SeriesCreationParametersDTO> setSeriesCreationParameters(BuildStructure structure,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            boolean startswithZeroScore, int[] discardingThresholds) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = new LinkedHashMap<String, SeriesCreationParametersDTO>();

        RegattaStructure regattaStructure = structure.getRegattaStructure();

        if (regattaStructure != null) {
            int i = 0;
            for (Series raceType : regattaStructure.getSeries()) {

                List<FleetDTO> fleets = getFleets(raceType.getFleets());
                setRaceNames(i, raceType, raceType.getFleets());

                seriesCreationParams.put(raceType.getSeries(), new SeriesCreationParametersDTO(fleets,
                /* medal */raceType.isMedal(), startswithZeroScore, firstColumnIsNonDiscardableCarryForward,
                        discardingThresholds, hasSplitFleetContiguousScoring));
                i++;
            }

        }
        return seriesCreationParams;

    }

    private List<String> setRaceNames(int i, Series raceType, ArrayList<Fleet> fleets) {
        List<String> raceNames = new ArrayList<String>();

        // set Racenumbers for each series
        Race[] races = fleets.get(raceType.getMaxIndex()).getRaces();
        for (int j = 0; j < races.length; j++) {
            Race race = races[j];
            setRacenumberStrategy.setRacenumber(race, raceType, i, raceNames);
        }
        return raceNames;
    }

    private List<FleetDTO> getFleets(ArrayList<Fleet> fleets) {
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

    private Color getColorFromString(final String colorString) {
        Color result = null;
        for (FleetColors fleetColor : FleetColors.values()) {
            if (fleetColor.name().equalsIgnoreCase(colorString)) {
                result = fleetColor.getColor();
                break;
            }
        }
        if (result == null) {
            result = Color.BLACK;
        }
        return result;
    }

    public void setCompetitors() {

        for (int i = 0; i < results.size(); i++) {
            BoatClass boatClass = null;

            boatClass = getBoatClass(selectedRegattas.get(i));

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

    private void parseRegattaXML(String url) throws FileNotFoundException, JAXBException, IOException {
        results.add(ParserFactory.INSTANCE.createParser(getInputStream(url), "").parse());
    }

    private InputStream getInputStream(String url) throws FileNotFoundException, IOException {
        URLConnection connection = new URL(url).openConnection();
        return connection.getInputStream();
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

    public List<BuildStructure> getBuildStructures() {
        return buildStructures;
    }

}
