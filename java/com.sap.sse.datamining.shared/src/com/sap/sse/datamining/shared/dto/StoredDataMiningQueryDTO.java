package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.UUID;

/** contains a storable data mining query */
public interface StoredDataMiningQueryDTO extends Serializable {

    public String getName();

    public UUID getId();

    public StatisticQueryDefinitionDTO getQuery();

}
