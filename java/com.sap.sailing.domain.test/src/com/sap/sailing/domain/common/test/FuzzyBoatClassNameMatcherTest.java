package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.FuzzyBoatClassNameMatcher;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class FuzzyBoatClassNameMatcherTest {
    private final FuzzyBoatClassNameMatcher matcher = new FuzzyBoatClassNameMatcher();
    
    @Test
    public void simpleFullMatchTest() {
        final List<Pair<String, Pair<String, Date>>> results = Arrays.asList(
                r("49er", "Kieler Woche 2023", TimePoint.now().asDate()),
                r("N17F", "Kieler Woche 2023", TimePoint.now().asDate()),
                r("ILCA6", "Kieler Woche 2023", TimePoint.now().asDate()));
        matcher.sortOfficialResultsByRelevance("N17F", results);
        assertEquals("N17F", results.get(0).getA());
    }

    private Pair<String, Pair<String, Date>> r(String boatClassNameFromResult, String eventNameFromResult, Date timeStamp) {
        return new Pair<>(boatClassNameFromResult, new Pair<>(eventNameFromResult, timeStamp));
    }
}
