package com.sap.sailing.simulator.test.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TracTracReaderExample {

    public static void main(String[] args) throws Exception {

        File dir = new File("C:\\Users\\i059829\\workspace\\sapsailingcapture\\java\\com.sap.sailing.simulator.test");
        String[] flist = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".data");
            }
        });

        // System.out.println(flist[1]);
        TracTracReader ttreader = new TracTracReaderFromFiles(flist);

        List<TrackedRace> lst = ttreader.read();

        for (TrackedRace r : lst) {
            RegattaAndRaceIdentifier id = r.getRaceIdentifier();
            System.out.println(id.getRegattaName() + " / " + id.getRaceName());
            for (Competitor c : r.getRace().getCompetitors()) {
                r.getTrack(c).lockForRead();
                for (GPSFixMoving gpsFix : r.getTrack(c).getFixes()) {
                    System.out.println(gpsFix.getTimePoint().asMillis());
                }
            }
        }

    }

}
