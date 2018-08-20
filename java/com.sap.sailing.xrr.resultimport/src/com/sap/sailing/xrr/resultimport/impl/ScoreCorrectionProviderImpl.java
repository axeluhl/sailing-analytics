package com.sap.sailing.xrr.resultimport.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.AbstractDocumentBasedScoreCorrectionProvider;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.schema.Division;
import com.sap.sailing.xrr.schema.Event;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class ScoreCorrectionProviderImpl extends AbstractDocumentBasedScoreCorrectionProvider {
    private static final Logger logger = Logger.getLogger(ScoreCorrectionProviderImpl.class.getName());
    private static final long serialVersionUID = -4596215011753860781L;

    private static final String name = "ISAF XML Regatta Result (XRR) Importer";
    
    private final ParserFactory parserFactory;
    
    public ScoreCorrectionProviderImpl(ResultDocumentProvider documentProvider, ParserFactory parserFactory) {
        super(documentProvider);
        this.parserFactory = parserFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Util.Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName()
            throws IOException, SAXException, ParserConfigurationException {
        Map<String, Set<Util.Pair<String, TimePoint>>> result = new HashMap<String, Set<Util.Pair<String, TimePoint>>>();
        for (Parser parser : getAllRegattaResults()) {
            try {
                RegattaResults regattaResult = parser.parse();
                TimePoint timePoint = XRRParserUtil.calculateTimePointForRegattaResults(regattaResult);
                for (Object o : regattaResult.getPersonOrBoatOrTeam()) {
                    if (o instanceof Event) {
                        Event event = (Event) o;
                        for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                            if (eventO instanceof Division) {
                                Division division = (Division) eventO;
                                String boatClassName = parser.getBoatClassName(division);
                                Set<Util.Pair<String, TimePoint>> set = result.get(event.getTitle());
                                if (set == null) {
                                    set = new HashSet<>();
                                    result.put(event.getTitle(), set);
                                }
                                set.add(new Util.Pair<String, TimePoint>(boatClassName, timePoint));
                            }
                        }
                    }
                }
            } catch (JAXBException e) {
                logger.info("Parse error during XRR import. Ignoring document "+parser.toString());
                logger.throwing(ScoreCorrectionProviderImpl.class.getName(), "getHasResultsForBoatClassFromDateByEventName", e);
            }
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws IOException, SAXException, ParserConfigurationException {
        for (Parser parser : getAllRegattaResults()) {
            try {
                RegattaResults regattaResults = parser.parse();
                TimePoint timePoint = XRRParserUtil.calculateTimePointForRegattaResults(regattaResults);
                if ((timePoint == null && timePointPublished == null)
                        || (timePoint != null && timePoint.equals(timePointPublished))) {
                    for (Object o : regattaResults.getPersonOrBoatOrTeam()) {
                        if (o instanceof Event) {
                            Event event = (Event) o;
                            if (event.getTitle().equals(eventName)) {
                                for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                                    if (eventO instanceof Division) {
                                        Division division = (Division) eventO;
                                        if (boatClassName.equals(parser.getBoatClassName(division))) {
                                            return new XRRRegattaResultsAsScoreCorrections(event, division, this,
                                                    parser);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JAXBException e) {
                logger.info("Parse error during XRR import. Ignoring document " + parser.toString());
                logger.throwing(ScoreCorrectionProviderImpl.class.getName(), "getHasResultsForBoatClassFromDateByEventName", e);
            }
        }
        return null;
    }

    private Iterable<Parser> getAllRegattaResults() throws SAXException, IOException,
            ParserConfigurationException {
        List<Parser> result = new ArrayList<>();
        for (ResultDocumentDescriptor resultDocDescr : getResultDocumentProvider().getResultDocumentDescriptors()) {
            Parser parser = parserFactory.createParser(resultDocDescr.getInputStream(), resultDocDescr.getDocumentName());
            result.add(parser);
        }
        return result;
    }

}
