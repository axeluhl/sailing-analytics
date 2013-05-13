package com.sap.sailing.barbados.resultimport.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.impl.AbstractDocumentBasedScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.RegattaScoreCorrectionsImpl;

public class ScoreCorrectionProviderImpl extends AbstractDocumentBasedScoreCorrectionProvider {
    public static final String BOATCLASS_NAME_METADATA_PROPERTY = "boatclassName";

    private static final long serialVersionUID = -5501186796881875686L;

    private static final String name = "Barbados 2013 505 Worlds Excel Result Importer";

    public ScoreCorrectionProviderImpl(ResultDocumentProvider resultDocumentProvider) {
        super(resultDocumentProvider);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String,TimePoint>>>();
        for (Triple<InputStream, String, TimePoint> inputStreamAndNameAndLastModified : getResultDocumentProvider().getDocumentsAndNamesAndLastModified()) {
            RegattaResults regattaResults = new BarbadosResultSpreadsheet(inputStreamAndNameAndLastModified.getA()).getRegattaResults(); 
            TimePoint lastModified = inputStreamAndNameAndLastModified.getC();
            final String boatClassName = getBoatClassName(regattaResults);
            result.put(boatClassName, Collections.singleton(new Pair<String, TimePoint>(boatClassName, lastModified)));
        }
        return result;
    }

    private String getBoatClassName(RegattaResults regattaResult) {
        Map<String, String> metadata = regattaResult.getMetadata();
        return metadata.get(BOATCLASS_NAME_METADATA_PROPERTY);
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePoint) throws Exception {
        for (Triple<InputStream, String, TimePoint> inputStreamAndNameAndLastModified : getResultDocumentProvider().getDocumentsAndNamesAndLastModified()) {
            RegattaResults regattaResults = new BarbadosResultSpreadsheet(inputStreamAndNameAndLastModified.getA()).getRegattaResults(); 
            if ((boatClassName == null && getBoatClassName(regattaResults) == null) || boatClassName.equals(getBoatClassName(regattaResults))) {
                return new RegattaScoreCorrectionsImpl(this, regattaResults);
            }
        }
        return null;
    }
}
