package com.sap.sailing.domain.polarsheets;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.impl.PolarSheetsHistogramDataImpl;

public class PolarSheetHistogramBuilder {

    private PolarSheetGenerationSettings settings;

    public PolarSheetHistogramBuilder(PolarSheetGenerationSettings settings) {
        this.settings = settings;
    }

    public PolarSheetsHistogramData build(List<Double> rawData, int angleIndex, double coefficiantOfVariation) {
        Double min = Collections.min(rawData);
        Double max = Collections.max(rawData);

        int numberOfColumns = settings.getNumberOfHistogramColumns();
        double range = (max - min) / numberOfColumns;
        Double[] xValues = new Double[numberOfColumns];
        for (int u = 0; u < numberOfColumns; u++) {
            xValues[u] = min + u * range + (0.5 * range);
        }

        Integer[] yValues = new Integer[numberOfColumns];
        for (Double dataPoint : rawData) {
            double notRoundedYet = (dataPoint - min) / range;
            int u = (int) notRoundedYet;
            if (u == numberOfColumns) {
                // For max value
                u = numberOfColumns - 1;
            }
            if (yValues[u] == null) {
                yValues[u] = 0;
            }
            yValues[u]++;
        }

        PolarSheetsHistogramData histogramData = new PolarSheetsHistogramDataImpl(angleIndex, xValues, yValues,
                rawData.size(), coefficiantOfVariation);
        return histogramData;
    }

}
