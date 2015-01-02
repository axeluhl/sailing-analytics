package com.sap.sailing.polars.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.PolarSheetsDataImpl;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.polars.data.BoatAndWindSpeedWithOriginInfo;
import com.sap.sailing.polars.data.DataPointWithOriginInfo;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.data.impl.BoatAndWindSpeedWithOriginInfoImpl;
import com.sap.sailing.polars.data.impl.DataPointWithOriginInfoImpl;

public class PolarSheetGenerator {

    private final List<List<BoatAndWindSpeedWithOriginInfo>> polarData;

    private WindSpeedSteppingWithMaxDistance stepping;

    private PolarSheetGenerationSettings settings;

    private final Set<PolarFix> fixes;

    public PolarSheetGenerator(Set<PolarFix> fixes, PolarSheetGenerationSettings settings) {
        this.settings = settings;
        this.fixes = fixes;
        stepping = settings.getWindSpeedStepping();
        polarData = initializePolarDataContainer();
    }

    private List<List<BoatAndWindSpeedWithOriginInfo>> initializePolarDataContainer() {
        List<List<BoatAndWindSpeedWithOriginInfo>> container = new ArrayList<List<BoatAndWindSpeedWithOriginInfo>>();
        for (int i = 0; i < 360; i++) {
            container.add(new ArrayList<BoatAndWindSpeedWithOriginInfo>());
        }
        return container;
    }


    private void addPolarFix(PolarFix polarFix) {
        long roundedAngleDeg = Math.round(polarFix.getTrueWindAngleDeg());
        Speed boatSpeed = polarFix.getBoatSpeed();
        Speed windSpeed = polarFix.getWindSpeed();
        int angleDeg = (int) roundedAngleDeg;
        if (angleDeg < 0) {
            angleDeg = (360 + angleDeg);
        }
        BoatAndWindSpeedWithOriginInfo speeds = new BoatAndWindSpeedWithOriginInfoImpl(boatSpeed, windSpeed,
                polarFix.getGaugeIdString(), polarFix.getDayString());
        polarData.get(angleDeg).add(speeds);
    }

    /**
     * 
     * @return results already averaged per angle. Also datacount overall and per angle. And a completion flag to show
     *         if the complete polar sheet has been generated or if the current results are only an intermediate
     *         product.
     */
    public PolarSheetsData generate() {

        for (PolarFix fix : fixes) {
            addPolarFix(fix);
        }

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
            BoatAndWindSpeedWithOriginInfo[] values = polarData.get(angleIndex).toArray(
                    new BoatAndWindSpeedWithOriginInfo[polarData.get(angleIndex).size()]);
            dataCount = dataCount + values.length;
            dataCountPerAngle[angleIndex] = values.length;
            double[] sumsPerWindSpeed = new double[levelCount];
            int[] dataCountPerWindSpeed = new int[levelCount];
            Map<Integer, List<DataPointWithOriginInfo>> dataSetForWindLevel = new HashMap<Integer, List<DataPointWithOriginInfo>>();
            gatherPolarData(values, sumsPerWindSpeed, dataCountPerWindSpeed, dataSetForWindLevel);

            calculateStatisticalMeasuresAndHistogramData(histogramBuilder, levelCount, averagedPolarDataByWindSpeed,
                    dataCountPerAngleForWindspeed, histogramDataMap, totalDataCountPerWindSpeed, angleIndex,
                    sumsPerWindSpeed, dataCountPerWindSpeed, dataSetForWindLevel);

        }

        for (int j = 0; j < levelCount; j++) {
            for (PolarSheetsHistogramData histogramData : histogramDataMap.get(j).values()) {
                Double polarSheetPointConfidenceMeasure = Math.min(1, histogramData.getCoefficiantOfVariation()
                        * (histogramData.getDataCount() / (double) totalDataCountPerWindSpeed.get(j)) * 80);
                histogramData.setConfidenceMeasure(polarSheetPointConfidenceMeasure);
            }
        }

        PolarSheetsData data = new PolarSheetsDataImpl(averagedPolarDataByWindSpeed, dataCount,
                dataCountPerAngleForWindspeed, stepping, histogramDataMap);

        return data;
    }

    private void gatherPolarData(BoatAndWindSpeedWithOriginInfo[] values, double[] sumsPerWindSpeed,
            int[] dataCountPerWindSpeed, Map<Integer, List<DataPointWithOriginInfo>> dataSetForWindLevel) {
        for (BoatAndWindSpeedWithOriginInfo singleDataPoint : values) {
            if (singleDataPoint != null) {
                double windSpeed = singleDataPoint.getWindSpeed().getKnots();
                int level = stepping.getLevelIndexForValue(windSpeed);
                if (level >= 0) {
                    double speed = singleDataPoint.getBoatSpeed().getKnots();
                    String windGaugesIdString = singleDataPoint.getWindGaugesIdString();
                    String dayString = singleDataPoint.getDayString();
                    if (!dataSetForWindLevel.containsKey(level)) {
                        dataSetForWindLevel.put(level, new ArrayList<DataPointWithOriginInfo>());
                    }
                    dataSetForWindLevel.get(level).add(
                            new DataPointWithOriginInfoImpl(speed, windGaugesIdString, dayString));
                    sumsPerWindSpeed[level] = sumsPerWindSpeed[level] + speed;
                    dataCountPerWindSpeed[level]++;
                }
            }
        }
    }

    private void calculateStatisticalMeasuresAndHistogramData(PolarSheetHistogramBuilder histogramBuilder,
            int levelCount, Number[][] averagedPolarDataByWindSpeed,
            Map<Integer, Integer[]> dataCountPerAngleForWindspeed,
            Map<Integer, Map<Integer, PolarSheetsHistogramData>> histogramDataMap,
            Map<Integer, Integer> totalDataCountPerWindSpeed, int angleIndex, double[] sumsPerWindSpeed,
            int[] dataCountPerWindSpeed, Map<Integer, List<DataPointWithOriginInfo>> dataSetForWindLevel) {
        for (int levelIndex = 0; levelIndex < levelCount; levelIndex++) {
            if (settings.shouldRemoveOutliers() && dataSetForWindLevel.get(levelIndex) != null) {
                List<DataPointWithOriginInfo> withoutOutliers = new ArrayList<DataPointWithOriginInfo>();
                Collections.sort(dataSetForWindLevel.get(levelIndex));
                if (dataCountPerWindSpeed[levelIndex] > /* Minimum data count for outlier detection */10) {
                    performOutlierExclusion(sumsPerWindSpeed, dataCountPerWindSpeed, dataSetForWindLevel,
                            levelIndex, withoutOutliers);
                }

            }

            if (dataCountPerWindSpeed[levelIndex] < settings.getMinimumDataCountPerAngle()) {
                dataSetForWindLevel.put(levelIndex, new ArrayList<DataPointWithOriginInfo>());
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
            List<DataPointWithOriginInfo> rawData = dataSetForWindLevel.get(levelIndex);
            if (rawData == null || rawData.size() <= 0) {
                continue;
            }
            PolarSheetsHistogramData histogramData = histogramBuilder.build(rawData, angleIndex,
                    coefficiantOfVariation);
            histogramDataMap.get(levelIndex).put(angleIndex, histogramData);

        }
    }

    private void performOutlierExclusion(double[] sumsPerWindSpeed, int[] dataCountPerWindSpeed,
            Map<Integer, List<DataPointWithOriginInfo>> dataSetForWindLevel, int levelIndex,
            List<DataPointWithOriginInfo> withoutOutliers) {
        for (int dataIndex = 0; dataIndex < dataSetForWindLevel.get(levelIndex).size(); dataIndex++) {
            DataPointWithOriginInfo dataPoint = dataSetForWindLevel.get(levelIndex).get(dataIndex);
            double pct = getNeighboorhoodSizePercentage(dataCountPerWindSpeed, dataSetForWindLevel,
                    levelIndex, dataIndex, dataPoint, settings);
            if (pct >= settings.getOutlierMinimumNeighborhoodPct()) {
                withoutOutliers.add(dataPoint);
            } else {
                sumsPerWindSpeed[levelIndex] = sumsPerWindSpeed[levelIndex] - dataPoint.getRawData();
                dataCountPerWindSpeed[levelIndex]--;
            }
        }
        dataSetForWindLevel.put(levelIndex, withoutOutliers);
    }

    /**
     * This method calculates the pct of the points neighborhoods based on a distance (in the settings) compared
     * to the complete dataset.
     */
    public static double getNeighboorhoodSizePercentage(int[] dataCountPerWindSpeed,
            Map<Integer, List<DataPointWithOriginInfo>> valuesInMap, int levelIndex, int dataIndex,
            DataPointWithOriginInfo dataPoint, PolarSheetGenerationSettings settings) {
      int neighborCount = 0;
        double pct = .0;

        int index = dataIndex;
        // Left side
        boolean done = false;
        while (!done) {
            index--;
            if (index < 0) {
                done = true;
            } else {
                Double dataPointToCheckForNeighborStatus = valuesInMap.get(levelIndex).get(
                        index).getRawData();
                if (dataPoint.getRawData() - dataPointToCheckForNeighborStatus <= settings
                        .getOutlierDetectionNeighborhoodRadius()) {
                    neighborCount++;
                } else {
                    done = true;
                }
                pct = (double) neighborCount / (double) dataCountPerWindSpeed[levelIndex];
                if (pct >= settings.getOutlierMinimumNeighborhoodPct()) {
                    done = true;
                }
            }
        }
        index = dataIndex;
        // Right side
        done = false;
        while (!done) {
            index++;
            if (index >= dataCountPerWindSpeed[levelIndex]) {
                done = true;
            } else {
                Double dataPointToCheckForNeighborStatus = valuesInMap.get(levelIndex).get(
                        index).getRawData();
                if (dataPointToCheckForNeighborStatus - dataPoint.getRawData() <= settings
                        .getOutlierDetectionNeighborhoodRadius()) {
                    neighborCount++;
                } else {
                    done = true;
                }
                pct = (double) neighborCount / (double) dataCountPerWindSpeed[levelIndex];
                if (pct >= settings.getOutlierMinimumNeighborhoodPct()) {
                    done = true;
                }
            }
        }
        pct = (double) neighborCount / (double) dataCountPerWindSpeed[levelIndex];
        return pct;
    }


    /**
     * Can be used to present more detailed data.
     * 
     * @return The complete set of datapoints that have been added. Can be quite big, depending on the amount of races
     *         and competitors.
     */
    public List<List<BoatAndWindSpeedWithOriginInfo>> getCompleteData() {
        return polarData;
    }

    public WindSpeedSteppingWithMaxDistance getStepping() {
        return stepping;
    }

}
