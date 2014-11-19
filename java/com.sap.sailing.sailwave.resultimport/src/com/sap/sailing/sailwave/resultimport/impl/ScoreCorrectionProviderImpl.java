package com.sap.sailing.sailwave.resultimport.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.AbstractDocumentBasedScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.RegattaScoreCorrectionsImpl;
import com.sap.sailing.sailwave.resultimport.CsvParser;
import com.sap.sailing.sailwave.resultimport.CsvParserFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class ScoreCorrectionProviderImpl extends AbstractDocumentBasedScoreCorrectionProvider {
    private static final long serialVersionUID = 4767200739966995306L;

    private static final String NAME = "Sailwave CSV result importer"; 

    private static final Logger logger = Logger.getLogger(ScoreCorrectionProviderImpl.class.getName());

    private final CsvParserFactory parserFactory;
    
    public ScoreCorrectionProviderImpl(ResultDocumentProvider documentProvider, CsvParserFactory parserFactory) {
        super(documentProvider);
        this.parserFactory = parserFactory;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Set<Util.Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception {
        Map<String, Set<Util.Pair<String, TimePoint>>> result = new HashMap<String, Set<Util.Pair<String, TimePoint>>>();
        for (CsvParser parser : getAllRegattaResults()) {
            try {
                parser.parseResults();
                String boatClass = parser.getBoatClass();
                
                result.put(/* use document name as "event" name */ parser.getFilename(),
                        Collections.singleton(new Util.Pair<String, TimePoint>(boatClass, parser.getLastModified())));

            } catch (Exception e) {
                logger.info("Parse error during CSV import. Ignoring document " + parser.toString());
                logger.throwing(ScoreCorrectionProviderImpl.class.getName(), "getHasResultsForBoatClassFromDateByEventName", e);
            }
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePointPublished) throws Exception {
        for (CsvParser parser : getAllRegattaResults()) {
            try {
                if(parser.getFilename().equals(eventName)) {
                    RegattaResults regattaResult = parser.parseResults();
                    return new RegattaScoreCorrectionsImpl(this, regattaResult);
                }
            } catch (Exception e) {
                logger.info("Parse error during CSV import. Ignoring document " + parser.toString());
                logger.throwing(ScoreCorrectionProviderImpl.class.getName(), "getScoreCorrections", e);
            }
        }
        return null;
    }

    private Iterable<CsvParser> getAllRegattaResults() throws Exception {
        List<CsvParser> result = new ArrayList<>();
        for (ResultDocumentDescriptor resultDocDescr : getResultDocumentProvider().getResultDocumentDescriptors()) {
            CsvParser parser = parserFactory.createParser(resultDocDescr.getInputStream(), resultDocDescr.getDocumentName(), resultDocDescr.getLastModified());
            result.add(parser);
        }
        return result;
    }
}
