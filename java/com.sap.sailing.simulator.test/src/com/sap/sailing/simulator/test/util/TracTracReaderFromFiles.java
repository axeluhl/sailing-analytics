package com.sap.sailing.simulator.test.util;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.tracking.TrackedRace;

public class TracTracReaderFromFiles implements TracTracReader {

    String[] dataFiles;

    @SuppressWarnings("unchecked")
    public List<TrackedRace> read() throws Exception {

        List<TrackedRace> lst = new ArrayList<TrackedRace>();

        for (String f : dataFiles) {
            List<TrackedRace> retrievedRacesList = null;
            FileInputStream f_in = new FileInputStream(f);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);
            retrievedRacesList = (ArrayList<TrackedRace>) obj_in.readObject();
            obj_in.close();
            lst.addAll(retrievedRacesList);
        }

        return lst;
    }

    public TracTracReaderFromFiles(String[] dataFiles) {
        this.dataFiles = dataFiles;
    }

}
