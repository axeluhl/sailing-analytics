package com.sap.sailing.kiworesultimport.impl;

import java.io.File;
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

    /**
     * In case the input stream contains more than one regatta summary, only the first one is returned
     * as a {@link RegattaScoreCorrections} object.
     */
    @Override
    public RegattaScoreCorrections getScoreCorrections(InputStream inputStream) throws Exception {
        final Iterable<RegattaSummary> regattaSummariesFromInputStream = getRegattaSummaries(inputStream);
        final RegattaScoreCorrections result;
        if (regattaSummariesFromInputStream == null || Util.isEmpty(regattaSummariesFromInputStream)) {
            result = null;
        } else {
            result = new RegattaSummaryAsScoreCorrections(regattaSummariesFromInputStream.iterator().next(), this);
        }
        return result;
    }

    private Iterable<RegattaSummary> getAllRegattaSummaries() throws IOException, SAXException, ParserConfigurationException {
        final List<RegattaSummary> result = new ArrayList<RegattaSummary>();
        for (ResultDocumentDescriptor resultDocDescr : getResultDocumentProvider().getResultDocumentDescriptors()) {
            if (resultDocDescr.getDocumentName().toLowerCase().endsWith(".zip")) {
                Util.addAll(getRegattaSummaries(resultDocDescr.getInputStream()), result);
            }
        }
        return result;
    }

    private Iterable<RegattaSummary> getRegattaSummaries(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        final ZipFileParser zipFileParser = ParserFactory.INSTANCE.createZipFileParser();
        final ZipFile zipFile = zipFileParser.parse(inputStream);
        return zipFile.getRegattaSummaries();
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
