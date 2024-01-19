package com.sap.sse.security.datamining.data;

import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.security.shared.SettingsAndPreferencesConstants;

public interface HasPreferenceContext {
    @Dimension(messageKey = "PreferenceName")
    String getPreferenceName();
    
    @Dimension(messageKey = "PreferenceBaseName")
    default String getPreferenceBaseName() {
        final int indexOfDocumentSeparator = getPreferenceName().indexOf(SettingsAndPreferencesConstants.DOCUMENT_SETTINGS_SUFFIX_SEPARATOR);
        return getPreferenceName().substring(0, indexOfDocumentSeparator < 0 ? getPreferenceName().length() : indexOfDocumentSeparator);
    }
    
    @Dimension(messageKey = "PreferenceValue")
    String getPreferenceValue();
    
    @Dimension(messageKey = "NumberOfObjectsContained")
    default int getNumberOfObjectsContainedDimension() {
        return getNumberOfObjectsContained();
    }

    @Statistic(messageKey = "NumberOfObjectsContained")
    int getNumberOfObjectsContained();
    
    @Statistic(messageKey = "PreferenceValueSize")
    default int getPreferenceValueSize() {
        return getPreferenceValue() == null ? 0 : getPreferenceValue().length();
    }
}
