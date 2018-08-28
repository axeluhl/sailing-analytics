package com.sap.sse.datamining.impl.components;

import java.util.Locale;

import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.IdentityFunction;
import com.sap.sse.datamining.shared.AdditionalResultData;
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
    
    private static final Function<?> IdentityFunction = new IdentityFunction();

    private int retrievedDataAmount;
    private String dataTypeMessageKey;
    private Function<?> extractionFunction;
    private String aggregationNameMessageKey;
    private int resultDecimals;
    
    /**
     * Creates a new builder with standard values for the additional data.
     */
    public OverwritingResultDataBuilder() {
        retrievedDataAmount = 0;
        resultDecimals = 0;
    }

    @Override
    public AdditionalResultData build(long calculationTimeInNanos, ResourceBundleStringMessages stringMessages, Locale locale) {
        String resultSignifier = buildResultSignifier(stringMessages, locale);
        return new AdditionalResultDataImpl(retrievedDataAmount, resultSignifier, resultDecimals, calculationTimeInNanos);
    }

    private String buildResultSignifier(ResourceBundleStringMessages stringMessages, Locale locale) {
        if (extractionFunction == null || aggregationNameMessageKey == null) {
            return "";
        }

        String extractedStatisticName;
        if (extractionFunction.equals(IdentityFunction)) {
            extractedStatisticName = stringMessages.get(locale, dataTypeMessageKey);
        } else {
            extractedStatisticName = extractionFunction.getLocalizedName(locale, stringMessages);
        }
        
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
    public void setDataTypeMessageKey(String dataTypeMessageKey) {
        this.dataTypeMessageKey = dataTypeMessageKey;
    }
    
    public String getDataTypeMessageKey() {
        return dataTypeMessageKey;
    }

    @Override
    public void setExtractionFunction(Function<?> extractionFunction) {
        this.extractionFunction = extractionFunction;
        setResultDecimals(extractionFunction.getResultDecimals());
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
