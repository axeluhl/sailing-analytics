package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface ExtractionFunctionProvider<SettingsType extends Settings>
        extends DataMiningComponentProvider<SettingsType> {

    public FunctionDTO getExtractionFunction();

    public void addExtractionFunctionChangedListener(ExtractionFunctionChangedListener listener);

}
