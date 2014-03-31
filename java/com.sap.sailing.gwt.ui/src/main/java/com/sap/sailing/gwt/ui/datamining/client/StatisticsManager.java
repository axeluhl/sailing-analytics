package com.sap.sailing.gwt.ui.datamining.client;

import java.util.Collection;
import java.util.Set;

import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.gwt.ui.datamining.client.selection.ComplexStatistic;

public interface StatisticsManager {

    public void addStatistic(ComplexStatistic statistic);

    public ComplexStatistic getStatistic(DataTypes dataType);
    public Collection<ComplexStatistic> getAllStatistics();

    public Set<DataTypes> getRegisteredBaseDataTypes();

}
