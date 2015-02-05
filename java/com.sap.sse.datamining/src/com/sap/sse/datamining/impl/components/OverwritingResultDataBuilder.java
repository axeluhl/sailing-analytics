package com.sap.sse.datamining.impl.components;

import java.util.Locale;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;
import com.sap.sse.i18n.ServerStringMessages;

/**
 * For general information see {@link AdditionalResultDataBuilder}.<br />
 * Conflict resolving strategy:
 * <ul>
 *      <li>Builds the sum of all given retrieved/filtered data amounts</li>
 *      <li>Other values are overwritten</li>
 * </ul>
 */
public class OverwritingResultDataBuilder implements AdditionalResultDataBuilder {

    private int retrievedDataAmount;
    private Function<?> extractionFunction;
    private String aggregationNameMessageKey;
    private Unit unit;
    private int resultDecimals;
    
    /**
     * Creates a new builder with standard values for the additional data.
     */
    public OverwritingResultDataBuilder() {
        retrievedDataAmount = 0;
        unit = Unit.None;
        resultDecimals = 0;
    }

    @Override
    public AdditionalResultData build(long calculationTimeInNanos, ServerStringMessages stringMessages, Locale locale) {
        return new AdditionalResultDataImpl(retrievedDataAmount, buildResultSignifier(stringMessages, locale), unit, resultDecimals, calculationTimeInNanos);
    }

    private String buildResultSignifier(ServerStringMessages stringMessages, Locale locale) {
        if (extractionFunction == null || aggregationNameMessageKey == null) {
            return "";
        }
        
        String extractedStatisticName = extractionFunction.getLocalizedName(locale, stringMessages);
        String aggregationName = stringMessages.get(locale, aggregationNameMessageKey);
        return stringMessages.get(locale, "ResultSignifier", extractedStatisticName, aggregationName);
    }

    @Override
    public void setRetrievedDataAmount(int retrievedDataAmount) {
        this.retrievedDataAmount = retrievedDataAmount;
    }

    @Override
    public int getRetrievedDataAmount() {
        return retrievedDataAmount;
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

}
