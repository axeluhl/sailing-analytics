package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;

public abstract class AbstractMarkPassingTestNew {

    private DynamicTrackedRace race;

    private final DetectorMarkPassing detector;

    protected AbstractMarkPassingTestNew(DetectorMarkPassing detector) throws MalformedURLException, URISyntaxException {

        this.detector = detector;

    }

    protected void compareMarkpasses() {

        int correctPasses = 0;

        Iterable<Competitor> competitors = race.getRace().getCompetitors();
        for (Competitor c : competitors) {

            NavigableSet<MarkPassing> givenMarkPasses = race.getMarkPassings(c);
          
            for (MarkPassing w : detector
                    .computeMarkpasses(race.getTrack(c), race.getRace().getCourse().getWaypoints())) {

                /*
                 * TODO compare each instance of given and calculated NavigableSet<MarkPassing> if( the same ){
                 * 
                 * correctPasses++;}
                 */

            }

        }

    }

    // TODO Calculate Accuracy = corractPasses / total MarkPassings

}
