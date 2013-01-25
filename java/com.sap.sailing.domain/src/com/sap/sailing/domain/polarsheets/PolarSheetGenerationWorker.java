package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.tracking.TrackedRace;

public class PolarSheetGenerationWorker {

    private boolean finished = false;

    private final Set<PerRaceWorker> workers;

    private final Map<Integer, List<Double>> polarData;

    public PolarSheetGenerationWorker(Set<TrackedRace> trackedRaces) {
        polarData = initializePolarDataContainer();
        workers = new HashSet<PerRaceWorker>();
        for (TrackedRace race : trackedRaces) {
            workers.add(new PerRaceWorker(this, race));
        }
    }

    private Map<Integer, List<Double>> initializePolarDataContainer() {
        HashMap<Integer, List<Double>> container = new HashMap<Integer, List<Double>>();
        for (int i = 0; i < 360; i++) {
            container.put(i, new ArrayList<Double>());
        }
        return container;
    }

    public void startPolarSheetGeneration() {
        for (PerRaceWorker worker : workers) {
            Thread workerThread = new Thread(worker);
            workerThread.start();
        }
    }

    public void addPolarData(long round, double normalizedSpeed) {
        polarData.get(round).add(normalizedSpeed);
    }

    public Map<Integer, Double> getPolarData() {
        Map<Integer, Double> averagedPolarData = new HashMap<Integer, Double>();
        for (Entry<Integer, List<Double>> entry : polarData.entrySet()) {
            double sum = 0;
            for (Double singleData : entry.getValue()) {
                sum = sum + singleData;
            }
            double average = sum / entry.getValue().size();
            averagedPolarData.put(entry.getKey(), average);
        }

        return averagedPolarData;
    }

    public void workerDone(PerRaceWorker worker) {
        if (workers.contains(worker)) {
            workers.remove(worker);
            if (workers.isEmpty()) {
                finished = true;
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

}
