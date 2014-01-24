package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Set;

import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.gwt.ui.datamining.selection.ComplexStatistic;

public interface StatisticsManager {

    public void addStatistic(ComplexStatistic statistic);

    public ComplexStatistic getStatistic(DataTypes dataType);
    public Collection<ComplexStatistic> getAllStatistics();

    public Set<DataTypes> getRegisteredBaseDataTypes();

}
