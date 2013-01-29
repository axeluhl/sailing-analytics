package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        for (int i = 0; i < 181; i++) {
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
        if (angle > 180) {
            angle = (360 - angle);
        }
        polarData.get(angle).add(normalizedSpeed);
    }

    public Number[] getPolarData() {
        Number[] averagedPolarData = new Number[360];
        for (int i = 0; i < 181; i++) {
            // Avoid Concurrent modification, lock would slow things down
            Double[] values = polarData.get(i).toArray(new Double[polarData.get(i).size()]);
            if (values.length < 20) {
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

        for (int i = 1; i < 180; i++) {
            if (averagedPolarData[i].doubleValue() == 0) {
                double next = 0;
                int currentIndex = i;
                while (next <= 0 && currentIndex < 181) {
                    currentIndex++;
                    if (averagedPolarData[currentIndex] != null) {
                        next = averagedPolarData[currentIndex].doubleValue();
                    }
                }
                averagedPolarData[i] = (averagedPolarData[i - 1].doubleValue() + next) / 2;
            }

            averagedPolarData[360 - i] = averagedPolarData[i];
        }

        return averagedPolarData;
    }

    public void workerDone(PerRaceWorker worker) {
        if (workers.contains(worker) && workerInitDone) {
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
