package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import com.sap.sailing.competitorimport.CompetitorDescriptor;
import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
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
    public Iterable<CompetitorDescriptor> getCompetitorDescriptors(String eventName, String regattaName) throws JAXBException, IOException {
        final List<CompetitorDescriptor> result = new ArrayList<>();
        final Map<String, CompetitorDescriptor> resultsByTeamID = new HashMap<>();
        final Map<String, CompetitorDescriptor> teamsWithoutRaceAssignments = new HashMap<>();
        for (ResultDocumentDescriptor resultDocDescr : getDocumentProvider().getResultDocumentDescriptors()) {
            if (resultDocDescr.getEventName().equals(eventName) &&
                    (regattaName == null || regattaName.equals(resultDocDescr.getRegattaName()))) {
                final Parser parser = getParserFactory().createParser(resultDocDescr.getInputStream(), resultDocDescr.getEventName());
                final RegattaResults regattaResults = parser.parse();
                for (Object o : regattaResults.getPersonOrBoatOrTeam()) {
                    if (o instanceof Team) {
                        final Team team = (Team) o;
                        teamsWithoutRaceAssignments.put(team.getTeamID(),
                                createCompetitorDescriptor(team, parser, /* event */ null, /* division */ null, /* raceID */ null));
                    } else if (o instanceof Event) {
                        Event event = (Event) o;
                        for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                            if (eventO instanceof Division) {
                                Division division = (Division) eventO;
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
        List<com.sap.sailing.domain.base.Person> persons = team.getCrew().stream().sorted((c1, c2) -> -c1.getPosition().name().compareTo(c2.getPosition().name())).map((crew)->{
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
                com.sap.sailing.domain.base.Person person = new PersonImpl(name, nationality, /* date of birth */ null, /* description */ xrrPerson.getGender().name());
                return person;
        }).collect(Collectors.toList());
        final CompetitorDescriptor competitorDescriptor = new CompetitorDescriptor(
                            event==null?null:event.getTitle(),
                            division==null?null:(division.getTitle() + (division.getGender() == null ? "" : division.getGender().name())),
                            race != null ? race.getRaceName() : null, /* fleetName */ null, sailNumber, team.getTeamName(),
                            teamNationality[0] == null ? null : teamNationality[0].getCountryCode(), persons);
        return competitorDescriptor;
    }

    protected CompetitorDocumentProvider getDocumentProvider() {
        return documentProvider;
    }
}
