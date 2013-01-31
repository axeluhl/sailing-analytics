package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

public class PolarSheetGenerationWorker {

    private final Set<RunnableFuture<Void>> workers;

    private final List<List<Double>> polarData;

    private final Executor executor;

    public PolarSheetGenerationWorker(Set<TrackedRace> trackedRaces, Executor executor) {
        polarData = initializePolarDataContainer();
        this.executor = executor;
        workers = new HashSet<RunnableFuture<Void>>();
        for (TrackedRace race : trackedRaces) {
            TimePoint startTime = race.getStartOfRace();
            TimePoint endTime = race.getEndOfRace();
            if (endTime == null) {
                //TODO Figure out if there is an alternative:
                endTime = race.getTimePointOfNewestEvent();
            }
            RaceDefinition raceDefinition = race.getRace();
            Iterable<Competitor> competitors = raceDefinition.getCompetitors();

            
            for (Competitor competitor : competitors) {
                RunnableFuture<Void> futureTask = new FutureTask<Void>(new PerRaceAndCompetitorPolarSheetGenerationWorker(race, this, startTime, endTime, competitor));
                workers.add(futureTask);
            }
           
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
        for (RunnableFuture<Void> future : workers) {
            executor.execute(future);
        }
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
        boolean complete = true;
        for (RunnableFuture<Void> future : workers) {
            if (!future.isDone()) {
                complete = false;
                break;
            }
        }
        
        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarData, complete, dataCount, dataCountPerAngle);

        
        return data;
    }

    public List<List<Double>> getCompleteData() {
        return polarData;
    }


}
