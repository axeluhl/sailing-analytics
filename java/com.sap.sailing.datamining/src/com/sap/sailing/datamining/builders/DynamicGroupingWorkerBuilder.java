package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.GroupingWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.DynamicGroupingWorker;

public class DynamicGroupingWorkerBuilder<DataType> implements WorkerBuilder<GroupingWorker<DataType>> {

    private String scriptText;
    private BaseBindingProvider<DataType> baseBindingProvider;

    public DynamicGroupingWorkerBuilder(String scriptText, BaseBindingProvider<DataType> baseBindingProvider) {
        this.scriptText = scriptText;
        this.baseBindingProvider = baseBindingProvider;
    }

    @Override
    public GroupingWorker<DataType> build() {
        return new DynamicGroupingWorker<DataType>(scriptText, baseBindingProvider);
    }

}
