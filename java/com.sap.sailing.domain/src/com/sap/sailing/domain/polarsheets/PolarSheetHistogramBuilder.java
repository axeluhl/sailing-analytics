package com.sap.sailing.domain.polarsheets;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.impl.PolarSheetsHistogramDataImpl;

public class PolarSheetHistogramBuilder {

    private PolarSheetGenerationSettings settings;

    public PolarSheetHistogramBuilder(PolarSheetGenerationSettings settings) {
        this.settings = settings;
    }

    public PolarSheetsHistogramData build(List<DataPointWithOriginInfo> dataPointsWithOriginInfo, int angleIndex,
            double coefficiantOfVariation) {
        DataPointWithOriginInfo minPoint = Collections.min(dataPointsWithOriginInfo);
        DataPointWithOriginInfo maxPoint = Collections.max(dataPointsWithOriginInfo);

        Double min = minPoint.getRawData();
        Double max = maxPoint.getRawData();

        int numberOfColumns = settings.getNumberOfHistogramColumns();
        double range = (max - min) / numberOfColumns;
        Double[] xValues = new Double[numberOfColumns];
        for (int u = 0; u < numberOfColumns; u++) {
            xValues[u] = min + u * range + (0.5 * range);
        }

        Map<String, Integer[]> yValuesByGaugeIds = new HashMap<String, Integer[]>();
        Integer[] yValues = new Integer[numberOfColumns];
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = 0;
        }
        for (DataPointWithOriginInfo dataPoint : dataPointsWithOriginInfo) {
            double notRoundedYet = (dataPoint.getRawData() - min) / range;
            int u = (int) notRoundedYet;
            if (u == numberOfColumns) {
                // For max value
                u = numberOfColumns - 1;
            }
            yValues[u]++;

            String gaugeIdsString = dataPoint.getWindGaugeIdString();
            if (!yValuesByGaugeIds.containsKey(gaugeIdsString)) {
                Integer[] yValuesForOneGaugeIdsString = new Integer[numberOfColumns];
                for (int i = 0; i < yValuesForOneGaugeIdsString.length; i++) {
                    yValuesForOneGaugeIdsString[i] = 0;
                }
                yValuesByGaugeIds.put(gaugeIdsString, yValuesForOneGaugeIdsString);
            }
            Integer[] yValuesForThatGaugeIdsString = yValuesByGaugeIds.get(gaugeIdsString);
            yValuesForThatGaugeIdsString[u]++;

        }
        

        PolarSheetsHistogramData histogramData = new PolarSheetsHistogramDataImpl(angleIndex, xValues, yValues,
                yValuesByGaugeIds, dataPointsWithOriginInfo.size(), coefficiantOfVariation);
        return histogramData;
    }

}
