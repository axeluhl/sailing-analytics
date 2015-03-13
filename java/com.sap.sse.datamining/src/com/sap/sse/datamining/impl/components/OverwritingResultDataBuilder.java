package com.sap.sse.datamining.impl.components;

import java.util.Locale;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.impl.AdditionalResultDataImpl;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * For general information see {@link AdditionalResultDataBuilder}.<br />
 * Conflict resolving strategy:
 * <ul>
 *      <li>Old values will be overwritten from the new ones</li>
 * </ul>
 */
public class OverwritingResultDataBuilder implements AdditionalResultDataBuilder {

    private int retrievedDataAmount;
    private Function<?> extractionFunction;
    private String aggregationNameMessageKey;
    private Unit resultUnit;
    private int resultDecimals;
    
    /**
     * Creates a new builder with standard values for the additional data.
     */
    public OverwritingResultDataBuilder() {
        retrievedDataAmount = 0;
        resultUnit = Unit.None;
        resultDecimals = 0;
    }

    @Override
    public AdditionalResultData build(long calculationTimeInNanos, ResourceBundleStringMessages stringMessages, Locale locale) {
        String unitSignifier = buildUnitSignifier(stringMessages, locale);
        String resultSignifier = buildResultSignifier(stringMessages, locale);
        return new AdditionalResultDataImpl(retrievedDataAmount, resultSignifier, resultUnit, unitSignifier,
                resultDecimals, calculationTimeInNanos);
    }

    private String buildResultSignifier(ResourceBundleStringMessages stringMessages, Locale locale) {
        if (extractionFunction == null || aggregationNameMessageKey == null) {
            return "";
        }
        
        String extractedStatisticName = extractionFunction.getLocalizedName(locale, stringMessages);
        String aggregationName = stringMessages.get(locale, aggregationNameMessageKey);
        return stringMessages.get(locale, "ResultSignifier", extractedStatisticName, aggregationName);
    }

    private String buildUnitSignifier(ResourceBundleStringMessages stringMessages, Locale locale) {
        if (resultUnit == null || resultUnit == Unit.None) {
            return "";
        }
        
        return stringMessages.get(locale, resultUnit.toString());
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
        setResultUnit(extractionFunction.getResultUnit());
        setResultDecimals(extractionFunction.getResultDecimals());
    }
    
    @Override
    public void setResultUnit(Unit resultUnit) {
        this.resultUnit = resultUnit;
    }
    
    @Override
    public void setResultDecimals(int resultDecimals) {
        this.resultDecimals = resultDecimals;
    }

    @Override
    public void setAggregationNameMessageKey(String aggregationNameMessageKey) {
        this.aggregationNameMessageKey = aggregationNameMessageKey;
    }

}
