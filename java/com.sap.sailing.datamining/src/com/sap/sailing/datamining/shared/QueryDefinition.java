package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.LocaleInfo;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public interface QueryDefinition extends Serializable {

    public LocaleInfo getLocaleInfo();

    public GrouperType getGrouperType();

    public StatisticType getStatisticType();

    public AggregatorType getAggregatorType();

    public DataTypes getDataType();

    public String getCustomGrouperScriptText();

    public List<SharedDimension> getDimensionsToGroupBy();

    public Map<SharedDimension, Iterable<?>> getSelection();

}