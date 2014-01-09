package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.markpassingcalculation.CandidateChooser;
import com.sap.sailing.domain.markpassingcalculation.CandidateFinder;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class FlorianopolisStartOfRaceTest extends FlorianopolisMarkPassingTest {

    public FlorianopolisStartOfRaceTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    protected void testRace() {
        CandidateFinder finder = new CandidateFinder(getTrackedRace());
        CandidateChooser chooser = new CandidateChooser(getTrackedRace());

        int mistakes = 0;
        for (Competitor c : getRace().getCompetitors()) {
            List<GPSFix> fixes = new ArrayList<GPSFix>();
            try {
                getTrackedRace().getTrack(c).lockForRead();

                for (GPSFixMoving fix : getTrackedRace().getTrack(c).getFixes()) {
                    if (fix.getTimePoint().minus(120000).before(getTrackedRace().getStartOfRace())) {
                        fixes.add(fix);
                    }
                }
            } finally {
                getTrackedRace().getTrack(c).unlockAfterRead();
            }
            finder.calculateFixesAffectedByNewCompetitorFixes(c, fixes);
            chooser.calculateMarkPassDeltas(c, finder.getCandidateDeltas(c));
            System.out.println("\n"+c);
            Waypoint w1 = getRace().getCourse().getFirstWaypoint();
            boolean gotFirst=false;
            boolean gotOther=false;
            for (Waypoint w : getRace().getCourse().getWaypoints()) {
                System.out.println(getTrackedRace().getMarkPassing(c, w));
                if(w==w1){
                    gotFirst = (getTrackedRace().getMarkPassing(c, w) != null) ? true : false;
                } else {
                    if(getTrackedRace().getMarkPassing(c, w) != null){
                        gotOther = true;
                        break;
                    }
                }
            }
            if(!gotFirst||gotOther){
                mistakes++;
            }
        } 
        Assert.assertTrue(mistakes<1);
    }
}
