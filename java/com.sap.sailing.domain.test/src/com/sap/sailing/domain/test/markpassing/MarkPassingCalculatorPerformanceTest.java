package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.test.measurements.Measurement;
import com.sap.sailing.domain.test.measurements.MeasurementCase;
import com.sap.sailing.domain.test.measurements.MeasurementXMLFile;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MarkPassingCalculatorPerformanceTest extends AbstractMockedRaceMarkPassingTest {

    private static LinkedHashMap<String, Long> result = new LinkedHashMap<>();

    public MarkPassingCalculatorPerformanceTest() {
        super();
    }

    protected Random rnd = new Random();
    protected long time;

    @AfterClass
    public static void createXMLFile() throws IOException {
        MeasurementXMLFile performanceReport = new MeasurementXMLFile(MarkPassingCalculatorPerformanceTest.class);
        for (String key : result.keySet()) {
            MeasurementCase performanceReportCase = performanceReport.addCase(key);
            performanceReportCase.addMeasurement(new Measurement(key, result.get(key)));
        }
        performanceReport.write();
    }

    @Test
    public void testFinder() {
        CandidateFinder f = new CandidateFinder(trackedRace);
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
        System.out.println(time);
        Assert.assertTrue(time < 3000);
    }

    @Test
    public void testChooser() {
        long time = timeToAddCandidatesToChooser(100, 1, 25);
        System.out.println(time);
        result.put("ChooserPerformance", time);
        assertTrue(time < 2000);
    }

    private long timeToAddCandidatesToChooser(int numberOfTimesAdding, int numberToAddEachTime, int numberOfRepititions) {
        long totalTime = 0;
        for (int i = 0; i < numberOfRepititions; i++) {
            CandidateChooser c = new CandidateChooser(trackedRace);
            for (int j = 0; j < numberOfTimesAdding; j++) {
                List<Candidate> newCandidates = new ArrayList<>();
                for (int k = 0; k < numberToAddEachTime; k++) {
                    newCandidates.add(randomCan());
                }
                time = System.currentTimeMillis();
                c.calculateMarkPassDeltas(bob, new Util.Pair<Iterable<Candidate>, Iterable<Candidate>>(newCandidates, new ArrayList<Candidate>()));
                totalTime += System.currentTimeMillis() - time;
            }
        }
        return totalTime / numberOfRepititions;
    }

    private Candidate randomCan() {
        int id = rnd.nextInt(3);
        return new Candidate(id + 1, new MillisecondsTimePoint((long) (rnd.nextDouble() * 200000)),
                0.5 + 0.5 * rnd.nextDouble(),  Util.get(trackedRace.getRace().getCourse().getWaypoints(), id), true, true, null);
    }
}
