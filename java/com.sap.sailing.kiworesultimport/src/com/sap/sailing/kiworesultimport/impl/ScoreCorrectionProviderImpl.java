package com.sap.sailing.kiworesultimport.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.kiworesultimport.ParserFactory;
import com.sap.sailing.kiworesultimport.RegattaSummary;
import com.sap.sailing.kiworesultimport.ZipFile;
import com.sap.sailing.kiworesultimport.ZipFileParser;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.impl.AbstractFileBasedScoreCorrectionProvider;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class ScoreCorrectionProviderImpl extends AbstractFileBasedScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    private static final String name = "Kieler Woche Official Results by b+m";
    
    public ScoreCorrectionProviderImpl(File scanDir) {
        super(scanDir);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Util.Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName()
            throws IOException, SAXException, ParserConfigurationException {
        Map<String, Set<Util.Pair<String, TimePoint>>> result = new HashMap<String, Set<Util.Pair<String, TimePoint>>>();
        for (RegattaSummary regattaSummary : getAllRegattaSummaries()) {
            String eventName = regattaSummary.getEventName();
            Set<Util.Pair<String, TimePoint>> resultTimesForBoatClassNames = result.get(eventName);
            if (resultTimesForBoatClassNames == null) {
                resultTimesForBoatClassNames = new HashSet<Util.Pair<String, TimePoint>>();
                result.put(eventName, resultTimesForBoatClassNames);
            }
            resultTimesForBoatClassNames.add(new Util.Pair<String, TimePoint>(regattaSummary.getBoatClassName(),
                    regattaSummary.getTimePointPublished()));
        }
        return result;
    }

    private Iterable<RegattaSummary> getAllRegattaSummaries() throws IOException, SAXException, ParserConfigurationException {
        List<RegattaSummary> result = new ArrayList<RegattaSummary>();
        ZipFileParser zipFileParser = ParserFactory.INSTANCE.createZipFileParser();
        for (ResultDocumentDescriptor resultDocDescr : getResultDocumentProvider().getResultDocumentDescriptors()) {
            if (resultDocDescr.getDocumentName().toLowerCase().endsWith(".zip")) {
                ZipFile zipFile = zipFileParser.parse(resultDocDescr.getInputStream());
                for (RegattaSummary regattaSummary : zipFile.getRegattaSummaries()) {
                    result.add(regattaSummary);
                }
            }
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws IOException, SAXException, ParserConfigurationException {
        for (RegattaSummary regattaSummary : getAllRegattaSummaries()) {
            if (regattaSummary.getEventName().equals(eventName) && regattaSummary.getBoatClassName().equals(boatClassName) &&
                    regattaSummary.getTimePointPublished().equals(timePointPublished)) {
                return new RegattaSummaryAsScoreCorrections(regattaSummary, this);
            }
        }
        return null;
    }

}
