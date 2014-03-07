package com.sap.sse.datamining;

import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.Unit;

/**
 * These builders are used to collect the additional result data from different sources (e.g. the Processors)
 * and resolves conflicts, if a member of the additional data gets set multiple times.
 */
public interface AdditionalResultDataBuilder {

    public AdditionalResultData build(long calculationTimeInNanos, DataMiningStringMessages stringMessages, Locale locale);

    public void setRetrievedDataAmount(int retrievedDataAmount);

    public void setFilteredDataAmount(int filteredDataAmount);

    public void setExtractedStatisticNameMessageKey(String extractedStatisticNameMessageKey);

    public void setAggregationNameMessageKey(String aggregationNameMessageKey);

    public void setUnit(Unit unit);

    public void setValueDecimals(int valueDecimals);


}
