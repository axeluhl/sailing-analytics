package com.sap.sailing.odf.resultimport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.odf.resultimport.CumulativeResultDocumentProvider;
import com.sap.sailing.odf.resultimport.OdfBody;
import com.sap.sailing.odf.resultimport.OdfBodyParser;
import com.sap.sailing.odf.resultimport.ParserFactory;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    private static final String name = "SwissTiming On Venue Result System";
    
    /**
     * The directory that will be scanned for <code>.zip</code> files which will then be passed to
     * {@link ZipFileParser} for analysis.
     */
    private final CumulativeResultDocumentProvider documentProvider;

    private final ParserFactory parserFactory;
    
    public ScoreCorrectionProviderImpl(CumulativeResultDocumentProvider documentProvider, ParserFactory parserFactory) {
        this.documentProvider = documentProvider;
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
        for (OdfBody body : getAllOdfBodies()) {
            String boatClassName = body.getBoatClassName();
            Set<Pair<String, TimePoint>> set = result.get(boatClassName);
            if (set == null) {
                set = new HashSet<>();
                result.put(boatClassName, set);
            }
            set.add(new Pair<String, TimePoint>(body.getEventName(), body.getTimePoint()));
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws IOException, SAXException, ParserConfigurationException {
        for (OdfBody body : getAllOdfBodies()) {
            if (body.getEventName().equals(eventName) && body.getBoatClassName().equals(boatClassName) &&
                    body.getTimePoint().equals(timePointPublished)) {
                return new OdfBodyAsScoreCorrections(body, this);
            }
        }
        return null;
    }

    private Iterable<OdfBody> getAllOdfBodies() throws SAXException, IOException, ParserConfigurationException {
        List<OdfBody> result = new ArrayList<>();
        for (InputStream is : documentProvider.getAllAvailableCumulativeResultDocuments()) {
            OdfBodyParser parser = parserFactory.createOdfBodyParser();
            OdfBody body = parser.parse(is, getName());
            result.add(body);
        }
        return result;
    }

}
