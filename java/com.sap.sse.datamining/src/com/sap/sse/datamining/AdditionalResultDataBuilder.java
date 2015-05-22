package com.sap.sse.datamining;

import java.util.Locale;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.data.Unit;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * These builders are used to collect the additional result data from different sources (e.g. the Processors)
 * and resolves conflicts, if a member of the additional data gets set multiple times.
 */
public interface AdditionalResultDataBuilder {

    public AdditionalResultData build(long calculationTimeInNanos, ResourceBundleStringMessages stringMessages, Locale locale);

    public void setRetrievedDataAmount(int retrievedDataAmount);
    
    public int getRetrievedDataAmount();

    /**
     * Sets the used {@link Function} for the statistic extraction. Also sets the {@linkplain Unit result unit} and the amount of
     * decimals to the values of the <code>Function</code> ({@link Function#getResultUnit()} and {@link Function#getResultDecimals()}).
     * @param extractionFunction
     */
    public void setExtractionFunction(Function<?> extractionFunction);
    public void setResultUnit(Unit resultUnit);
    public void setResultDecimals(int resultDecimals);

    public void setAggregationNameMessageKey(String aggregationNameMessageKey);

}
