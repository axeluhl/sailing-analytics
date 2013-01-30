package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

public class PolarSheetGenerationWorker {

    private boolean finished = false;

    private final Set<PerRaceWorker> workers;

    private final List<List<Double>> polarData;

    private boolean workerInitDone = false;

    public PolarSheetGenerationWorker(Set<TrackedRace> trackedRaces) {
        polarData = initializePolarDataContainer();
        workers = new HashSet<PerRaceWorker>();
        for (TrackedRace race : trackedRaces) {
            workers.add(new PerRaceWorker(this, race));
        }
    }

    private List<List<Double>> initializePolarDataContainer() {
        List<List<Double>> container = new ArrayList<List<Double>>();
        for (int i = 0; i < 360; i++) {
            container.add(new ArrayList<Double>());
        }
        return container;
    }

    public void startPolarSheetGeneration() {
        for (PerRaceWorker worker : workers) {
            Thread workerThread = new Thread(worker);
            workerThread.start();
        }
        workerInitDone = true;
    }

    public void addPolarData(long roundedAngle, double normalizedSpeed) {
        int angle = (int) roundedAngle;
        if (angle < 0) {
            angle = (360 + angle);
        }
        polarData.get(angle).add(normalizedSpeed);
    }

    public PolarSheetsData getPolarData() {
        Number[] averagedPolarData = new Number[360];
        Integer[] dataCountPerAngle = new Integer[360];
        int dataCount = 0;
        for (int i = 0; i < 360; i++) {
            // Avoid Concurrent modification, lock would slow things down
            Double[] values = polarData.get(i).toArray(new Double[polarData.get(i).size()]);
            dataCount = dataCount + values.length;
            dataCountPerAngle[i] = values.length;
            if (values.length < 1) {
                averagedPolarData[i] = 0;
            } else {
                double sum = 0;
                for (Double singleData : values) {
                    if (singleData != null) {
                        sum = sum + singleData;
                    }
                }
                double average = sum / values.length;
                averagedPolarData[i] = average;
            }
        }
        
        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarData, finished, dataCount, dataCountPerAngle);

        
        return data;
    }

    public void workerDone(PerRaceWorker worker) {
        if (workers.contains(worker) && workerInitDone) {
            workers.remove(worker);
            if (workers.isEmpty()) {
                finished = true;
            }
        }
    }

}
