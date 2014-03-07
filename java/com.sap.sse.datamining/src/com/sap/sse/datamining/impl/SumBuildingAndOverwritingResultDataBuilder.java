package com.sap.sse.datamining.impl;

import java.util.Locale;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.Message;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;

/**
 * For general information see {@link AdditionalResultDataBuilder}.<br />
 * Conflict resolving strategy:
 * <ul>
 *      <li>Builds the sum of all given retrieved/filtered data amounts</li>
 *      <li>Other values are overwritten</li>
 * </ul>
 */
public class SumBuildingAndOverwritingResultDataBuilder implements AdditionalResultDataBuilder {

    private int retrievedDataAmount;
    private int filteredDataAmount;
    private String extractedStatisticNameMessageKey;
    private String aggregationNameMessageKey;
    private Unit unit;
    private int valueDecimals;
    
    /**
     * Creates a new builder with standard values for the additional data.
     */
    public SumBuildingAndOverwritingResultDataBuilder() {
        retrievedDataAmount = 0;
        filteredDataAmount = 0;
        unit = Unit.None;
        valueDecimals = 0;
    }

    @Override
    public AdditionalResultData build(long calculationTimeInNanos, DataMiningStringMessages stringMessages, Locale locale) {
        return new AdditionalResultDataImpl(retrievedDataAmount, filteredDataAmount, buildResultSignifier(stringMessages, locale), unit, valueDecimals, calculationTimeInNanos);
    }

    private String buildResultSignifier(DataMiningStringMessages stringMessages, Locale locale) {
        String extractedStatisticName = stringMessages.get(locale, extractedStatisticNameMessageKey);
        String aggregationName = stringMessages.get(locale, aggregationNameMessageKey);
        return stringMessages.get(locale, Message.ResultSignifier, extractedStatisticName, aggregationName);
    }

    @Override
    public void setRetrievedDataAmount(int retrievedDataAmount) {
        this.retrievedDataAmount += retrievedDataAmount;
    }

    @Override
    public void setFilteredDataAmount(int filteredDataAmount) {
        this.filteredDataAmount += filteredDataAmount;
    }

    @Override
    public void setExtractedStatisticNameMessageKey(String extractedStatisticNameMessageKey) {
        this.extractedStatisticNameMessageKey = extractedStatisticNameMessageKey;
    }

    @Override
    public void setAggregationNameMessageKey(String aggregationNameMessageKey) {
        this.aggregationNameMessageKey = aggregationNameMessageKey;
    }

    @Override
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public void setValueDecimals(int valueDecimals) {
        this.valueDecimals = valueDecimals;
    }

}
