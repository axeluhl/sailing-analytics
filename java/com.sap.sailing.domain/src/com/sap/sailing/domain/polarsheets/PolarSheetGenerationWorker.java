package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsHistogramDataImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsWindStepping;
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

    private PolarSheetsWindStepping stepping;

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
        Integer[] levels = { 4, 6, 8, 10, 12, 14, 16, 20, 25, 30 };
        stepping = new PolarSheetsWindStepping(levels);
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
                PerRaceAndCompetitorPolarSheetGenerationWorker task = new PerRaceAndCompetitorPolarSheetGenerationWorker(
                        race, this, startTime, endTime, competitor);
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

        // Do this before working through data, since it's possible that remaining data is processed by the workers
        // during the process of filling the result data.
        boolean complete = allWorkersDone();

        int levelCount = stepping.getNumberOfLevels();
        Number[][] averagedPolarDataByWindSpeed = new Number[levelCount][360];
        Integer[] dataCountPerAngle = new Integer[360];
        int dataCount = 0;
        Map<Integer, Integer[]> dataCountPerAngleForWindspeed = new HashMap<Integer, Integer[]>();
        Map<Integer, Map<Integer,PolarSheetsHistogramData>> histogramDataMap = new HashMap<Integer, Map<Integer,PolarSheetsHistogramData>>();
        Map<Integer, Integer> totalDataCountPerWindSpeed = new HashMap<Integer, Integer>();
        for (int i = 0; i < levelCount; i++) {
            dataCountPerAngleForWindspeed.put(i, new Integer[360]);
            histogramDataMap.put(i, new HashMap<Integer,PolarSheetsHistogramData>());
            totalDataCountPerWindSpeed.put(i, 0);
        }
        for (int i = 0; i < 360; i++) {
            Double[] varianceNominator = new Double[levelCount];
            // Avoid Concurrent modification of lists, so get current state as an array; lock would slow things down
            BoatAndWindSpeed[] values = polarData.get(i).toArray(new BoatAndWindSpeed[polarData.get(i).size()]);
            dataCount = dataCount + values.length;
            dataCountPerAngle[i] = values.length;
            double[] sumsPerWindSpeed = new double[levelCount];
            int[] dataCountPerWindSpeed = new int[levelCount];
            Map<Integer, List<Double>> dataSetForWindLevel = new HashMap<Integer, List<Double>>();
            for (BoatAndWindSpeed singleDataPoint : values) {
                if (singleDataPoint != null) {
                    double windSpeed = singleDataPoint.getWindSpeed().getKnots();
                    int level = stepping.getLevelIndexForValue(windSpeed);
                    if (level < 0) {
                        continue;
                    }
                    double speed = singleDataPoint.getBoatSpeed().getKnots();
                    if (!dataSetForWindLevel.containsKey(level)) {
                        dataSetForWindLevel.put(level, new ArrayList<Double>());
                    }
                    dataSetForWindLevel.get(level).add(speed);
                    sumsPerWindSpeed[level] = sumsPerWindSpeed[level] + speed;
                    varianceNominator[level] = (varianceNominator[level] == null) ? speed : varianceNominator[level]
                            + speed;
                    dataCountPerWindSpeed[level]++;
                }
            }
           
            for (int j = 0; j < levelCount; j++) {
                totalDataCountPerWindSpeed.put(j, totalDataCountPerWindSpeed.get(j) + dataCountPerWindSpeed[j]);
                Double average = sumsPerWindSpeed[j] / dataCountPerWindSpeed[j];
                if (average.isNaN()) {
                    average = new Double(0);
                }
                averagedPolarDataByWindSpeed[j][i] = average;
                dataCountPerAngleForWindspeed.get(j)[i] = dataCountPerWindSpeed[j];
                double coefficiantOfVariation = 0;
                if (dataCountPerWindSpeed[j] > 0 && average > 0) {
                    Double variance = varianceNominator[j] / dataCountPerWindSpeed[j];
                    Double standardDeviation = Math.sqrt(variance);
                    coefficiantOfVariation = standardDeviation / average;
                } 
                List<Double> rawData = dataSetForWindLevel.get(j);
                if (rawData == null || rawData.size() <= 0) {
                    continue;
                }
                Double min = Collections.min(rawData);
                Double max = Collections.max(rawData);
                //TODO make number of columns dynamic to chart size
                int numberOfColumns = 20;
                double range = (max - min) / numberOfColumns;
                Double[] xValues = new Double[numberOfColumns];
                for (int u = 0; u < numberOfColumns; u++) {
                    xValues[u] = min + u * range + ( 0.5 * range);
                }

                Integer[] yValues = new Integer[numberOfColumns];
                for (Double dataPoint : rawData) {
                    int u = (int) (((dataPoint - min) / range));
                    if (u == numberOfColumns) {
                        //For max value
                        u = 19;
                    }
                    if (yValues[u] == null) {
                        yValues[u] = 0;
                    }
                    yValues[u]++;
                }

                PolarSheetsHistogramData histogramData = new PolarSheetsHistogramDataImpl(i, xValues, yValues, rawData.size(), coefficiantOfVariation);
                histogramDataMap.get(j).put(i, histogramData);
                
            }

        }
        
        for (int j = 0; j < levelCount; j++) {
            for (PolarSheetsHistogramData histogramData : histogramDataMap.get(j).values()) {
                Double polarSheetPointConfidenceMeasure = (1 - histogramData.getCoefficiantOfVariation()) * 
                        (histogramData.getDataCount() / (double) totalDataCountPerWindSpeed.get(j)) * 360;
                histogramData.setConfidenceMeasure(polarSheetPointConfidenceMeasure);
            }
        }

        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, complete, dataCount,
                dataCountPerAngleForWindspeed, stepping, histogramDataMap);

        return data;
    }

    private boolean allWorkersDone() {
        boolean complete = true;
        for (PerRaceAndCompetitorPolarSheetGenerationWorker task : workers) {
            if (!task.isDone()) {
                complete = false;
                break;
            }
        }
        return complete;
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

    public PolarSheetsWindStepping getStepping() {
        return stepping;
    }

}
