package com.sap.sse.datamining.impl.components;

import java.util.Locale;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.functions.Function;
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
    private Function<?> extractionFunction;
    private String aggregationNameMessageKey;
    private Unit unit;
    private int resultDecimals;
    
    /**
     * Creates a new builder with standard values for the additional data.
     */
    public SumBuildingAndOverwritingResultDataBuilder() {
        retrievedDataAmount = 0;
        unit = Unit.None;
        resultDecimals = 0;
    }

    @Override
    public AdditionalResultData build(long calculationTimeInNanos, DataMiningStringMessages stringMessages, Locale locale) {
        return new AdditionalResultDataImpl(retrievedDataAmount, buildResultSignifier(stringMessages, locale), unit, resultDecimals, calculationTimeInNanos);
    }

    private String buildResultSignifier(DataMiningStringMessages stringMessages, Locale locale) {
        if (extractionFunction == null || aggregationNameMessageKey == null) {
            return "";
        }
        
        String extractedStatisticName = extractionFunction.getLocalizedName(locale, stringMessages);
        String aggregationName = stringMessages.get(locale, aggregationNameMessageKey);
        return stringMessages.get(locale, Message.ResultSignifier.toString(), extractedStatisticName, aggregationName);
    }

    @Override
    public void setRetrievedDataAmount(int retrievedDataAmount) {
        this.retrievedDataAmount += retrievedDataAmount;
    }

    @Override
    public void setExtractionFunction(Function<?> extractionFunction) {
        this.extractionFunction = extractionFunction;
        unit = extractionFunction.getResultUnit();
        resultDecimals = extractionFunction.getResultDecimals();
    }

    @Override
    public void setAggregationNameMessageKey(String aggregationNameMessageKey) {
        this.aggregationNameMessageKey = aggregationNameMessageKey;
    }

    protected int getRetrievedDataAmount() {
        return retrievedDataAmount;
    }

    protected Function<?> getExtractionFunction() {
        return extractionFunction;
    }

    protected String getAggregationNameMessageKey() {
        return aggregationNameMessageKey;
    }

    protected Unit getUnit() {
        return unit;
    }

    protected int getResultDecimals() {
        return resultDecimals;
    }

}
