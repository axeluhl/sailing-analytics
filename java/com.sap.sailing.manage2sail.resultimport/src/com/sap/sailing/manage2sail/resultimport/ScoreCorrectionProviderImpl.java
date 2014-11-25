package com.sap.sailing.manage2sail.resultimport;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.impl.XRRRegattaResultsAsScoreCorrections;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Event;
import com.sap.sailing.xrr.resultimport.schema.EventGender;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider, ResultUrlProvider {
    private static final Logger logger = Logger.getLogger(ScoreCorrectionProviderImpl.class.getName());
    private static final long serialVersionUID = 222663322974305822L;

    public static final String NAME = "Manage2Sail XRR Result Importer";

    private final ParserFactory parserFactory;
    private final ResultUrlRegistry resultUrlRegistry;
    private final ResultDocumentProvider documentProvider;
    
    public ScoreCorrectionProviderImpl(ResultDocumentProvider documentProvider, ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        this.documentProvider = documentProvider;
        this.parserFactory = parserFactory;
        this.resultUrlRegistry = resultUrlRegistry;
    }

    public ScoreCorrectionProviderImpl(ParserFactory parserFactory, ResultUrlRegistry resultUrlRegistry) {
        this.documentProvider = new Manage2SailResultDocumentProvider(this);
        this.parserFactory = parserFactory;
        this.resultUrlRegistry = resultUrlRegistry;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Set<Util.Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() {
        Map<String, Set<Util.Pair<String, TimePoint>>> result = new HashMap<String, Set<Util.Pair<String,TimePoint>>>();
        try {
            for (ResultDocumentDescriptor resultDocDescr : documentProvider.getResultDocumentDescriptors()) {
                String eventName = resultDocDescr.getEventName() != null ? resultDocDescr.getEventName() : resultDocDescr.getRegattaName();
                String boatClass = resultDocDescr.getBoatClass();
                if(boatClass != null && eventName != null) {
                    if(resultDocDescr.getCompetitorGenderType() != null) {
                        boatClass += ", " + resultDocDescr.getCompetitorGenderType().name();
                    }
                    Set<Util.Pair<String, TimePoint>> eventResultsSet = result.get(eventName);
                    if(eventResultsSet == null) {
                        eventResultsSet = new HashSet<Util.Pair<String, TimePoint>>();
                        result.put(eventName, eventResultsSet);
                    }
                    eventResultsSet.add(new Util.Pair<String, TimePoint>(boatClass, resultDocDescr.getLastModified()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws IOException, SAXException, ParserConfigurationException {
        Parser parser = resolveParser(eventName, boatClassName);
        try {
            RegattaResults regattaResults = parser.parse();
//            TimePoint timePoint = XRRParserUtil.calculateTimePointForRegattaResults(regattaResults);
//            if ((timePoint == null && timePointPublished == null) || (timePoint != null && timePoint.equals(timePointPublished))) {
                for (Object o : regattaResults.getPersonOrBoatOrTeam()) {
                    if (o instanceof Event) {
                        Event event = (Event) o;
                        if (event.getTitle().equals(eventName)) {
                            for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                                if (eventO instanceof Division) {
                                    Division division = (Division) eventO;
                                    EventGender divisionGender = division.getGender();
                                    String divisionBoatClassAndGender = parser.getBoatClassName(division);
                                    if(divisionGender != null) {
                                        divisionBoatClassAndGender += ", " + divisionGender.name();  
                                    }
                                    if (boatClassName.equalsIgnoreCase(divisionBoatClassAndGender) || boatClassName.contains(divisionBoatClassAndGender)) {
                                        return new XRRRegattaResultsAsScoreCorrections(event, division, this,
                                                parser);
                                    }
                                }
                            }
                        }
                    }
//                }
            }
        } catch (JAXBException e) {
            logger.info("Parse error during XRR import. Ignoring document " + parser.toString());
            logger.throwing(ScoreCorrectionProviderImpl.class.getName(), "getHasResultsForBoatClassFromDateByEventName", e);
        }
        return null;
    }
    
    private Parser resolveParser(String eventName, String boatClassName) throws IOException {
        Parser result = null;
        for (ResultDocumentDescriptor resultDocDescr : documentProvider.getResultDocumentDescriptors()) {
            String boatClassAndGenderType = resultDocDescr.getBoatClass();
            if(resultDocDescr.getCompetitorGenderType() != null) {
                boatClassAndGenderType += ", " + resultDocDescr.getCompetitorGenderType().name();
            }
            if(eventName.equals(resultDocDescr.getEventName()) && boatClassName.equals(boatClassAndGenderType)) {
                result = parserFactory.createParser(resultDocDescr.getInputStream(), resultDocDescr.getEventName());
                break;
            }
        }
        return result;
    }

    @Override
    public Iterable<URL> getUrls() {
        return resultUrlRegistry.getResultUrls(NAME);
    }
}
