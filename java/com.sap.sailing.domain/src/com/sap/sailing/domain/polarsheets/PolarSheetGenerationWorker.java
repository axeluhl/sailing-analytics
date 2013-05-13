package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Allows extracting data about average speed for each angle creating the foundation of polar-sheet generation. Tasks
 * are assigned to the executor per race & per competitor and the results are filled on the go to allow progress
 * indication in the polar-sheet itself.
 * 
 * @author D054528 Frederik Petersen
 * 
 */
public class PolarSheetGenerationWorker {

    private final Set<PerRaceAndCompetitorPolarSheetGenerationWorker> workers;

    private final List<List<BoatAndWindSpeed>> polarData;

    private final Executor executor;

    /**
     * Will prepare the {@link PerRaceAndCompetitorPolarSheetGenerationWorker}s per race & per competitor. This includes
     * determining start and end time.
     * 
     * @param trackedRaces
     *            from which the data is to be collected
     * @param executor
     *            executes the tasks upon {@link #startPolarSheetGeneration()}
     */
    public PolarSheetGenerationWorker(Set<TrackedRace> trackedRaces, Executor executor) {
        polarData = initializePolarDataContainer();
        this.executor = executor;
        workers = new HashSet<PerRaceAndCompetitorPolarSheetGenerationWorker>();
        for (TrackedRace race : trackedRaces) {
            TimePoint startTime = race.getStartOfRace();
            TimePoint endTime = race.getEndOfRace();
            if (endTime == null) {
                // TODO Figure out if there is an alternative:
                endTime = race.getTimePointOfNewestEvent();
            }
            RaceDefinition raceDefinition = race.getRace();
            Iterable<Competitor> competitors = raceDefinition.getCompetitors();

            for (Competitor competitor : competitors) {
                PerRaceAndCompetitorPolarSheetGenerationWorker task = new PerRaceAndCompetitorPolarSheetGenerationWorker(race, this, startTime, endTime, competitor);
                workers.add(task);
            }

        }
    }

    private List<List<BoatAndWindSpeed>> initializePolarDataContainer() {
        List<List<BoatAndWindSpeed>> container = new ArrayList<List<BoatAndWindSpeed>>();
        for (int i = 0; i < 360; i++) {
            container.add(new ArrayList<BoatAndWindSpeed>());
        }
        return container;
    }

    /**
     * Starts the {@link PerRaceAndCompetitorPolarSheetGenerationWorker}s
     */
    public void startPolarSheetGeneration() {
        for (Runnable task : workers) {
            executor.execute(task);
        }
    }

    /**
     * To be called from {@link PerRaceAndCompetitorPolarSheetGenerationWorker} for adding a datapoint to the list of
     * results
     * 
     * @param roundedAngle
     *            boat's angle to the wind
     * @param boatSpeed
     *            boat's speed in knots
     * @param windSpeed
     *            wind's speed in knots
     */
    protected void addPolarData(long roundedAngle, Speed boatSpeed, Speed windSpeed) {
        int angle = (int) roundedAngle;
        if (angle < 0) {
            angle = (360 + angle);
        }
        BoatAndWindSpeed speeds = new BoatAndWindSpeedImpl(boatSpeed, windSpeed);
        polarData.get(angle).add(speeds);
    }

    /**
     * 
     * @return results already averaged per angle. Also datacount overall and per angle. And a completion flag to show
     *         if the complete polar sheet has been generated or if the current results are only an intermediate
     *         product.
     */
    public PolarSheetsData getPolarData() {
        Number[][] averagedPolarDataByWindSpeed = new Number[13][360];
        Integer[] dataCountPerAngle = new Integer[360];
        int dataCount = 0;
        Map<Integer, Integer[]> dataCountPerAngleForWindspeed = new HashMap<Integer, Integer[]>();
        for (int i = 0; i < 13; i++) {
            dataCountPerAngleForWindspeed.put(i, new Integer[360]);
        }
        for (int i = 0; i < 360; i++) {
            // Avoid Concurrent modification, lock would slow things down
            BoatAndWindSpeed[] values = polarData.get(i).toArray(new BoatAndWindSpeed[polarData.get(i).size()]);
            dataCount = dataCount + values.length;
            dataCountPerAngle[i] = values.length;
            double[] sumsPerWindSpeed = new double[13];
            int[] dataCountPerWindSpeed = new int[13];
            for (BoatAndWindSpeed singleDataPoint : values) {
                if (singleDataPoint != null) {
                    int windSpeed = (int) singleDataPoint.getWindSpeed().getBeaufort();
                    //Somehow beaufort sometimes gets bigger than twelve. TODO: Investigation
                    if (windSpeed > 12) {
                        windSpeed = 12;
                    }
                    //TODO enable different kinds of metrics for boats speed
                    sumsPerWindSpeed[windSpeed] = sumsPerWindSpeed[windSpeed] + singleDataPoint.getBoatSpeed().getKnots();
                    dataCountPerWindSpeed[windSpeed]++;
                }
            }
            
            for (int j = 0; j < 13; j++) {
                Double average = sumsPerWindSpeed[j] / dataCountPerWindSpeed[j];
                if (average.isNaN()) {
                    average = new Double(0);
                }
                averagedPolarDataByWindSpeed[j][i] = average;
                dataCountPerAngleForWindspeed.get(j)[i] = dataCountPerWindSpeed[j];
            }
            
            
            

        }
        boolean complete = true;
        for (PerRaceAndCompetitorPolarSheetGenerationWorker task : workers) {
            if (!task.isDone()) {
                complete = false;
                break;
            }
        }

        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, complete, dataCount,
                dataCountPerAngleForWindspeed);

        return data;
    }

    /**
     * Can be used to present more detailed data.
     * 
     * @return The complete set of datapoints that have been added. Can be quite big, depending on the amount of races
     *         and competitors.
     */
    public List<List<BoatAndWindSpeed>> getCompleteData() {
        return polarData;
    }

}
