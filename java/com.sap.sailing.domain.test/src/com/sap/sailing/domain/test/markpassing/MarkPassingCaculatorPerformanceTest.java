package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.markpassingcalculation.AbstractCandidateFinder;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.test.measurements.Measurement;
import com.sap.sailing.domain.test.measurements.MeasurementCase;
import com.sap.sailing.domain.test.measurements.MeasurementXMLFile;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class MarkPassingCaculatorPerformanceTest extends AbstractMockedRaceMarkPassingTest {

    private static LinkedHashMap<String, Long> result = new LinkedHashMap<>();
    private static String className;

    public MarkPassingCaculatorPerformanceTest() {
        super();
        className = getClass().getName();
    }

    @AfterClass
    public static void createXMLFile() throws IOException {
        MeasurementXMLFile performanceReport = new MeasurementXMLFile("TEST-MarkPassingCaculatorPerformanceTest.xml", "MarkPassingCaculatorPerformanceTest", className);
        for (String key : result.keySet()) {
            MeasurementCase performanceReportCase = performanceReport.addCase(key);
            performanceReportCase.addMeasurement(new Measurement(key, result.get(key)));
        }
        performanceReport.write();
    }

    @Test
    public void testFinder() {
        AbstractCandidateFinder f = new CandidateFinder(trackedRace);
        List<GPSFix> fixesAdded = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            GPSFixMoving fix = rndFix();
            trackedRace.recordFix(bob, fix);
            fixesAdded.add(fix);
        }
        time = System.currentTimeMillis();
        f.getCandidateDeltas(bob, fixesAdded);
        time = System.currentTimeMillis() - time;
        result.put("FinderPerformance", time);
        Assert.assertTrue(time < 2000);
    }

    @Test
    public void testChooser() {
        long time = timeToAddCandidatesToChooser(500, 1);
        result.put("ChooserPerformance", time);
        assertTrue(time < 800);
    }

    private long timeToAddCandidatesToChooser(int numberOfCandidates, int numberToAdd) {
        ArrayList<Candidate> newCans = new ArrayList<>();
        for (int i = 0; i < numberOfCandidates; i++) {
            newCans.add(randomCan());
        }
        time = System.currentTimeMillis();
        CandidateChooser c = new CandidateChooser(trackedRace);
        c.calculateMarkPassDeltas(bob, new Pair<Iterable<Candidate>, Iterable<Candidate>>(newCans, new ArrayList<Candidate>()));
        time = System.currentTimeMillis() - time;
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ArrayList<Candidate> old = new ArrayList<>();
            ArrayList<Candidate> ne = new ArrayList<>();
            for (int j = 0; j < numberToAdd; j++) {
                ne.add(randomCan());
            }
            time = System.currentTimeMillis();
            c.calculateMarkPassDeltas(bob, new Pair<Iterable<Candidate>, Iterable<Candidate>>(ne, old));
            times.add(System.currentTimeMillis() - time);
            old = ne;
        }
        long total = 0;
        for (long l : times) {
            total = total + l;
        }
        return total / times.size();
    }

    private Candidate randomCan() {
        int id = rnd.nextInt(3);
        return new Candidate(id + 1, new MillisecondsTimePoint((long) (System.currentTimeMillis() - 300000 + (Math.random() * (7800000)))), 0.5 + 0.5 * Math.random(),
                waypoints.get(id), true, true, "Test");
    }
}
