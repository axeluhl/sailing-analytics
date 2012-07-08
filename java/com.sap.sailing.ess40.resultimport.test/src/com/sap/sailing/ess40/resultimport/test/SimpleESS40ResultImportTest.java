package com.sap.sailing.ess40.resultimport.test;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.ess40.resultimport.impl.ScoreCorrectionProviderImpl;

public class SimpleESS40ResultImportTest {
    @Test
    public void testProviderFound() throws Exception {
        ScoreCorrectionProvider scp = new ScoreCorrectionProviderImpl();
        Map<String, Set<Pair<String, TimePoint>>> hasResult = scp.getHasResultsForBoatClassFromDateByEventName();
        assertTrue(hasResult.containsKey("qingdao"));
    }

    @Test
    public void testQingdaoResults() throws Exception {
        ScoreCorrectionProvider scp = new ScoreCorrectionProviderImpl();
        Map<String, Set<Pair<String, TimePoint>>> hasResult = scp.getHasResultsForBoatClassFromDateByEventName();
        RegattaScoreCorrections qingdaoResults = scp.getScoreCorrections("qingdao", "extreme 40", hasResult.get("qingdao").iterator().next().getB());
        assertNotNull(qingdaoResults);
        // TODO continue here and require results to match what we see in the CSV
    }
}
