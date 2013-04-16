package com.sap.sailing.xrr.resultimport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.AbstractDocumentBasedScoreCorrectionProvider;
import com.sap.sailing.xrr.resultimport.Parser;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.schema.Division;
import com.sap.sailing.xrr.resultimport.schema.Event;
import com.sap.sailing.xrr.resultimport.schema.RegattaResults;

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
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName()
            throws IOException, SAXException, ParserConfigurationException {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String, TimePoint>>>();
        for (Parser parser : getAllRegattaResults()) {
            try {
                RegattaResults regattaResult = parser.parse();
                TimePoint timePoint = getTimePointForRegattaResults(regattaResult);
                for (Object o : regattaResult.getPersonOrBoatOrTeam()) {
                    if (o instanceof Event) {
                        Event event = (Event) o;
                        for (Object eventO : event.getRaceOrDivisionOrRegattaSeriesResult()) {
                            if (eventO instanceof Division) {
                                Division division = (Division) eventO;
                                String boatClassName = parser.getBoatClassName(division);
                                Set<Pair<String, TimePoint>> set = result.get(event.getTitle());
                                if (set == null) {
                                    set = new HashSet<>();
                                    result.put(event.getTitle(), set);
                                }
                                set.add(new Pair<String, TimePoint>(boatClassName, timePoint));
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

    private TimePoint getTimePointForRegattaResults(RegattaResults regattaResult) {
        XMLGregorianCalendar date = regattaResult.getDate();
        XMLGregorianCalendar time = regattaResult.getTime();
        date.setHour(time.getHour());
        date.setMinute(time.getMinute());
        date.setSecond(time.getSecond());
        date.setMillisecond(time.getMillisecond());
        date.setTimezone(0);
        TimePoint timePoint = new MillisecondsTimePoint(date.toGregorianCalendar().getTime());
        return timePoint;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws IOException, SAXException, ParserConfigurationException {
        for (Parser parser : getAllRegattaResults()) {
            try {
                RegattaResults regattaResults = parser.parse();
                TimePoint timePoint = getTimePointForRegattaResults(regattaResults);
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
        for (Triple<InputStream, String, TimePoint> is : getResultDocumentProvider().getDocumentsAndNamesAndLastModified()) {
            Parser parser = parserFactory.createParser(is.getA(), is.getB());
            result.add(parser);
        }
        return result;
    }

}
