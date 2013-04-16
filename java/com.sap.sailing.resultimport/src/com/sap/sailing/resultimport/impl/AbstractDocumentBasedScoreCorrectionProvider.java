package com.sap.sailing.resultimport.impl;

import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.resultimport.ResultDocumentProvider;

public abstract class AbstractDocumentBasedScoreCorrectionProvider implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -207867446958976110L;
    private final ResultDocumentProvider resultDocumentProvider;

    public AbstractDocumentBasedScoreCorrectionProvider(ResultDocumentProvider resultDocumentProvider) {
        this.resultDocumentProvider = resultDocumentProvider;
    }

    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName, TimePoint timePoint)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    protected ResultDocumentProvider getResultDocumentProvider() {
        return resultDocumentProvider;
    }
}
