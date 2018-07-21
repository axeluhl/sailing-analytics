package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.schema.Boat;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.Event;
import com.sap.sailing.xrr.schema.Person;
import com.sap.sailing.xrr.schema.Race;
import com.sap.sailing.xrr.schema.RaceResult;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sailing.xrr.schema.SeriesResult;
import com.sap.sailing.xrr.schema.Team;

public class CompetitorImporter extends AbstractManage2SailProvider implements CompetitorProvider {
    private static final long serialVersionUID = 7389956404604333931L;
    private static final Logger logger = Logger.getLogger(CompetitorImporter.class.getName());
    private final CompetitorDocumentProvider documentProvider;

    public CompetitorImporter(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        super(parserFactory, resultUrlRegistry);
        documentProvider = new CompetitorDocumentProvider(this);
    }
    
    @Override
    public Map<String, Set<String>> getHasCompetitorsForRegattasInEvent() throws IOException {
        Map<String, Set<String>> result = new HashMap<>();
        for (ResultDocumentDescriptor resultDocDescr : getDocumentProvider().getResultDocumentDescriptors()) {
            final String eventName = resultDocDescr.getEventName();
            final String regattaName = resultDocDescr.getRegattaName();
            Set<String> set = result.get(eventName);
            if (set == null) {
                set = new HashSet<>();
                result.put(eventName, set);
            }
            set.add(regattaName);
        }
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Iterable<CompetitorDescriptor> getCompetitorDescriptors(String eventName, String regattaName) throws JAXBException, IOException {
        final List<CompetitorDescriptor> result = new ArrayList<>();
        final Map<String, CompetitorDescriptor> resultsByTeamID = new HashMap<>();
        final Map<String, CompetitorDescriptor> teamsWithoutRaceAssignments = new HashMap<>(); // keys are the teamID
        for (ResultDocumentDescriptor resultDocDescr : getDocumentProvider().getResultDocumentDescriptors()) {
            if (resultDocDescr.getEventName().equals(eventName) &&
                    (regattaName == null || regattaName.equals(resultDocDescr.getRegattaName()))) {
                final Parser parser = getParserFactory().createParser(resultDocDescr.getInputStream(), resultDocDescr.getEventName());
                try {
                    final RegattaResults regattaResults = parser.parse();
                    // If teams are found outside of a Division context then no boat class would be assigned to the
                    // competitor. However, if only one Division exists it can be used as default and the boat class
                    // can therefore be derived from it. This, however, is not clear until the document has been fully
                    // consumed. So we keep track of the teams found outside of Divisions, and if exactly one division
                    // is found we create the CompetitorDescriptors as if they were part of that Division; otherwise
                    // we need to come up with a default boat class instead.
                    final Set<Team> teamsOutsideOfDivision = new HashSet<>();
                    Map<Division, Event> divisions = new HashMap<>();
                    for (Object o : regattaResults.getPersonOrBoatOrTeam()) {
                        if (o instanceof Team) {
                            final Team team = (Team) o;
                            teamsOutsideOfDivision.add(team);
                        } else if (o instanceof Event) {
                            Event event = (Event) o;
                            for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                                if (eventO instanceof Division) {
                                    Division division = (Division) eventO;
                                    divisions.put(division, event);
                                    if (regattaName == null || regattaName.equals(division.getTitle())) {
                                        for (Object divisionO : division.getSeriesResultOrRaceResultOrTRResult()) {
                                            String raceID = null;
                                            Team team = null;
                                            if (divisionO instanceof RaceResult) {
                                                RaceResult raceResult = (RaceResult) divisionO;
                                                raceID = raceResult.getRaceID();
                                                team = parser.getTeam(raceResult.getTeamID());
                                            } else if (divisionO instanceof SeriesResult) {
                                                SeriesResult seriesResult = (SeriesResult) divisionO;
                                                team = parser.getTeam(seriesResult.getTeamID());
                                            }
                                            if (team != null) {
                                                final CompetitorDescriptor competitorDescriptor = createCompetitorDescriptor(
                                                        team, parser, event, division, raceID);
                                                resultsByTeamID.put(team.getTeamID(), competitorDescriptor);
                                                result.add(competitorDescriptor);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (final Team teamOutsideOfDivision : teamsOutsideOfDivision) {
                        if (divisions.size() == 1) { // exactly one Division; use as default for teams outside of division that were not assigned to a division later
                            if (!resultsByTeamID.containsKey(teamOutsideOfDivision.getTeamID())) {
                                final CompetitorDescriptor competitorDescriptor = createCompetitorDescriptor(
                                        teamOutsideOfDivision, parser, divisions.values().iterator().next(), divisions.keySet().iterator().next(), /* raceID */ null);
                                resultsByTeamID.put(teamOutsideOfDivision.getTeamID(), competitorDescriptor);
                                result.add(competitorDescriptor);
                            }
                        } else {
                            teamsWithoutRaceAssignments.put(teamOutsideOfDivision.getTeamID(),
                                    createCompetitorDescriptor(teamOutsideOfDivision, parser, /* event */ null, /* division */ null, /* raceID */ null));
                        }
                    }
                } catch (JAXBException e) {
                    logger.log(Level.WARNING, "Exception trying to read competitors for event "+resultDocDescr.getEventName()+
                            ", regatta "+resultDocDescr.getRegattaName()+" from document "+resultDocDescr.getDocumentName(), e);
                }
            }
        }
        for (final Map.Entry<String, CompetitorDescriptor> tid : teamsWithoutRaceAssignments.entrySet()) {
            if (!resultsByTeamID.containsKey(tid.getKey())) {
                result.add(tid.getValue());
            }
        }
        return result;
    }

    private CompetitorDescriptor createCompetitorDescriptor(Team team, final Parser parser, Event event,
            Division division, String raceID) {
        final Boat boat = parser.getBoat(team.getBoatID());
        final String sailNumber = boat.getSailNumber();
        final Race race = parser.getRace(raceID);
        final Nationality[] teamNationality = new Nationality[] {
            // use that of team; if not defined for team, use first nationality of a team member that has one defined
            team.getNOC() == null ? null : new NationalityImpl(team.getNOC().name())
        };
        final String boatClassName = parser.getBoatClassName(division);
        List<PersonDTO> persons = team.getCrew().stream().sorted((c1, c2) -> -c1.getPosition().name().compareTo(c2.getPosition().name())).map((crew)->{
                Person xrrPerson = parser.getPerson(crew.getPersonID());
                String name = xrrPerson.getGivenName()+" "+xrrPerson.getFamilyName();
                final Nationality nationality;
                if (xrrPerson.getNOC() == null) {
                    nationality = null;
                } else {
                    nationality = new NationalityImpl(xrrPerson.getNOC().name());
                    if (teamNationality[0] == null) {
                        teamNationality[0] = nationality;
                    }
                }
                PersonDTO person = new PersonDTO(
                        name, /* dateOfBirth */ null, /* description */ xrrPerson.getGender()==null?null:xrrPerson.getGender().name(),
                                nationality==null?null:nationality.getCountryCode().getThreeLetterIOCCode());
                return person;
        }).collect(Collectors.toList());
        UUID competitorUUID;
        try {
            competitorUUID = UUID.fromString(team.getTeamID());
        } catch (IllegalArgumentException e) {
            competitorUUID = null;
        }
        UUID boatUUID;
        try {
            boatUUID = UUID.fromString(boat.getBoatID());
        } catch (IllegalArgumentException e) {
            boatUUID = null;
        }
        final CompetitorDescriptor competitorDescriptor = new CompetitorDescriptor(
                            event == null ? null : event.getTitle(),
                            division == null ? null : (division.getTitle() + (division.getGender() == null ? "" : division.getGender().name())),
                            race != null ? race.getRaceName() : null, /* fleetName */ null, competitorUUID, /* name */ team.getTeamName(),
                            /* short name */ null, /* team name */ team.getTeamName(), persons, 
                            teamNationality[0] == null ? null : teamNationality[0].getCountryCode(), /* timeOnTimeFactor */ null,
                            /* timeOnDistanceAllowancePerNauticalMile */ null, boatUUID, boat.getBoatName(), boatClassName, sailNumber);
        return competitorDescriptor;
    }

    protected CompetitorDocumentProvider getDocumentProvider() {
        return documentProvider;
    }
}
