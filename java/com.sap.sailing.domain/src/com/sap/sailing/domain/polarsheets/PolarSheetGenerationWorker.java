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
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
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

    private PolarSheetGenerationSettings settings;

    /**
     * Will prepare the {@link PerRaceAndCompetitorPolarSheetGenerationWorker}s per race & per competitor. This includes
     * determining start and end time.
     * 
     * @param trackedRaces
     *            from which the data is to be collected
     * @param settings 
     * @param executor
     *            executes the tasks upon {@link #startPolarSheetGeneration()}
     */
    public PolarSheetGenerationWorker(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) {
        this.settings = settings;
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
                        race, this, startTime, endTime, competitor, settings);
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
        
        PolarSheetHistogramBuilder histogramBuilder = new PolarSheetHistogramBuilder(settings);

        int levelCount = stepping.getNumberOfLevels();
        Number[][] averagedPolarDataByWindSpeed = new Number[levelCount][360];
        Integer[] dataCountPerAngle = new Integer[360];
        int dataCount = 0;
        Map<Integer, Integer[]> dataCountPerAngleForWindspeed = new HashMap<Integer, Integer[]>();
        Map<Integer, Map<Integer, PolarSheetsHistogramData>> histogramDataMap = new HashMap<Integer, Map<Integer, PolarSheetsHistogramData>>();
        Map<Integer, Integer> totalDataCountPerWindSpeed = new HashMap<Integer, Integer>();
        for (int levelIndex = 0; levelIndex < levelCount; levelIndex++) {
            dataCountPerAngleForWindspeed.put(levelIndex, new Integer[360]);
            histogramDataMap.put(levelIndex, new HashMap<Integer, PolarSheetsHistogramData>());
            totalDataCountPerWindSpeed.put(levelIndex, 0);
        }
        for (int angleIndex = 0; angleIndex < 360; angleIndex++) {
            // Avoid Concurrent modification of lists, so get current state as an array; lock would slow things down
            BoatAndWindSpeed[] values = polarData.get(angleIndex).toArray(
                    new BoatAndWindSpeed[polarData.get(angleIndex).size()]);
            dataCount = dataCount + values.length;
            dataCountPerAngle[angleIndex] = values.length;
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
                    dataCountPerWindSpeed[level]++;
                }
            }

            for (int levelIndex = 0; levelIndex < levelCount; levelIndex++) {
                if (settings.shouldRemoveOutliers()) {
                    List<Double> withoutOutliers = new ArrayList<Double>();
                    Double average = sumsPerWindSpeed[levelIndex] / dataCountPerWindSpeed[levelIndex];
                    if (average.isNaN()) {
                        average = new Double(0);
                    }
                    double standardDeviation;
                    if (dataCountPerWindSpeed[levelIndex] > 0 && average > 0) {
                        Double variance = sumsPerWindSpeed[levelIndex] / dataCountPerWindSpeed[levelIndex];
                        standardDeviation = Math.sqrt(variance);
                        for (int dataIndex = 0; dataIndex < dataSetForWindLevel.get(levelIndex).size(); dataIndex++) {
                            Double dataPoint = dataSetForWindLevel.get(levelIndex).get(dataIndex);
                            if (dataPoint > average - settings.getOutlierDetectionFactor() * standardDeviation
                                    && dataPoint < average + settings.getOutlierDetectionFactor() * standardDeviation) {
                                withoutOutliers.add(dataPoint);
                            } else {
                                sumsPerWindSpeed[levelIndex] = sumsPerWindSpeed[levelIndex] - dataPoint;
                                dataCountPerWindSpeed[levelIndex]--;
                            }
                        }
                        dataSetForWindLevel.put(levelIndex, withoutOutliers);
                    }
                    
                }
                
                if (dataCountPerWindSpeed[levelIndex] < settings.getMinimumDataCountPerAngle()) {
                    dataSetForWindLevel.put(levelIndex, new ArrayList<Double>());
                    sumsPerWindSpeed[levelIndex] = 0;
                    dataCountPerWindSpeed[levelIndex] = 0;
                }
                totalDataCountPerWindSpeed.put(levelIndex, totalDataCountPerWindSpeed.get(levelIndex)
                        + dataCountPerWindSpeed[levelIndex]);
                Double average = sumsPerWindSpeed[levelIndex] / dataCountPerWindSpeed[levelIndex];
                if (average.isNaN()) {
                    average = new Double(0);
                }
                averagedPolarDataByWindSpeed[levelIndex][angleIndex] = average;
                dataCountPerAngleForWindspeed.get(levelIndex)[angleIndex] = dataCountPerWindSpeed[levelIndex];
                double coefficiantOfVariation = 0;
                if (dataCountPerWindSpeed[levelIndex] > 0 && average > 0) {
                    Double variance = sumsPerWindSpeed[levelIndex] / dataCountPerWindSpeed[levelIndex];
                    Double standardDeviation = Math.sqrt(variance);
                    coefficiantOfVariation = standardDeviation / average;
                }
                List<Double> rawData = dataSetForWindLevel.get(levelIndex);
                if (rawData == null || rawData.size() <= 0) {
                    continue;
                }
                PolarSheetsHistogramData histogramData = histogramBuilder.build(rawData, angleIndex, coefficiantOfVariation);
                histogramDataMap.get(levelIndex).put(angleIndex, histogramData);
                
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
