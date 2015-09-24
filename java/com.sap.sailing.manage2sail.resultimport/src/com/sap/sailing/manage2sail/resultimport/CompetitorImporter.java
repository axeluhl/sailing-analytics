package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

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

public class CompetitorImporter extends AbstractManage2SailProvider {
    private final CompetitorDocumentProvider documentProvider;

    public CompetitorImporter(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        super(parserFactory, resultUrlRegistry);
        documentProvider = new CompetitorDocumentProvider(this);
    }

    public Iterable<CompetitorDescriptor> getCompetitorDescriptors() throws JAXBException, IOException {
        final List<CompetitorDescriptor> result = new ArrayList<>();
        for (ResultDocumentDescriptor resultDocDescr : documentProvider.getResultDocumentDescriptors()) {
            final Parser parser = getParserFactory().createParser(resultDocDescr.getInputStream(), resultDocDescr.getEventName());
            final RegattaResults regattaResults = parser.parse();
            for (Object o : regattaResults.getPersonOrBoatOrTeam()) {
                if (o instanceof Event) {
                    Event event = (Event) o;
                    for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                        if (eventO instanceof Division) {
                            Division division = (Division) eventO;
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
                                            event.getTitle(), division.getTitle() + (division.getGender() == null ? "" : division.getGender().name()),
                                            race != null ? race.getRaceName() : null, /* fleetName */ null, sailNumber, team.getTeamName(),
                                                    teamNationality[0] == null ? null : teamNationality[0].getCountryCode(), persons);
                                    result.add(competitorDescriptor);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
