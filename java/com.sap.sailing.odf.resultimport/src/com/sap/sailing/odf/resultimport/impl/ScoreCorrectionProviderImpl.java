package com.sap.sailing.odf.resultimport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
import com.sap.sailing.odf.resultimport.RegattaSummary;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    private static final String name = "SwissTiming On Venue Result System";
    
    /**
     * The directory that will be scanned for <code>.zip</code> files which will then be passed to
     * {@link ZipFileParser} for analysis.
     */
    private final CumulativeResultDocumentProvider documentProvider;
    
    public ScoreCorrectionProviderImpl(CumulativeResultDocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName()
            throws IOException, SAXException, ParserConfigurationException {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String, TimePoint>>>();
        // TODO find out how we can tell "all" available ODF results
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws IOException, SAXException, ParserConfigurationException {
        // TODO find out 
        for (RegattaSummary regattaSummary : getAllRegattaSummaries()) {
            if (regattaSummary.getEventName().equals(eventName) && regattaSummary.getBoatClassName().equals(boatClassName) &&
                    regattaSummary.getTimePointPublished().equals(timePointPublished)) {
                return new RegattaSummaryAsScoreCorrections(regattaSummary, this);
            }
        }
        return null;
    }

    private Iterable<RegattaSummary> getAllRegattaSummaries() throws SAXException, IOException, ParserConfigurationException {
        for (InputStream is : documentProvider.getAllAvailableCumulativeResultDocuments()) {
            OdfBodyParser parser = new OdfBodyParserImpl();
            OdfBody body = parser.parse(is, getName());
        }
        // TODO Auto-generated method stub
        return null;
    }

}
