package com.sap.sse.datamining.shared.impl.dto;

import java.util.UUID;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;

/** contains a storable data mining query */
public class StoredDataMiningQueryDTOImpl implements StoredDataMiningQueryDTO {

    private static final long serialVersionUID = -5119084914429627047L;

    private String name;

    private UUID id;

    private StatisticQueryDefinitionDTO query;

    public StoredDataMiningQueryDTOImpl() {
    }

    public StoredDataMiningQueryDTOImpl(String name, UUID id, StatisticQueryDefinitionDTO query) {
        this.name = name;
        this.id = id;
        this.query = query;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public StatisticQueryDefinitionDTO getQuery() {
        return query;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSerializedQuery(StatisticQueryDefinitionDTO query) {
        this.query = query;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoredDataMiningQueryDTOImpl other = (StoredDataMiningQueryDTOImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
