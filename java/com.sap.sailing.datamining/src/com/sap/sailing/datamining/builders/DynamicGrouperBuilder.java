package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.GroupingWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.DynamicGrouper;

public class DynamicGrouperBuilder<DataType> implements WorkerBuilder<GroupingWorker<DataType>> {

    private String scriptText;
    private BaseBindingProvider<DataType> baseBindingProvider;

    public DynamicGrouperBuilder(String scriptText, BaseBindingProvider<DataType> baseBindingProvider) {
        this.scriptText = scriptText;
        this.baseBindingProvider = baseBindingProvider;
    }

    @Override
    public GroupingWorker<DataType> build() {
        return new DynamicGrouper<DataType>(scriptText, baseBindingProvider);
    }

}
