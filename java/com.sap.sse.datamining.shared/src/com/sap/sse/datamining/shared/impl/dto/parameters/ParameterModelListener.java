package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;

import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;

public interface ParameterModelListener {
    void parameterAdded(DataMiningReportDTO report, FilterDimensionParameter parameter);
    void parameterRemoved(DataMiningReportDTO report, FilterDimensionParameter parameter);
    void parameterValueChanged(FilterDimensionParameter parameter, Iterable<? extends Serializable> oldValues);
}
