package com.sap.sse.datamining.shared.impl.dto;

import java.util.UUID;

import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningReportDTO;

public class StoredDataMiningReportDTOImpl implements StoredDataMiningReportDTO {
    private static final long serialVersionUID = 9218620680326470175L;
    
    private final UUID id;
    private String name;
    private DataMiningReportDTO report;

    public StoredDataMiningReportDTOImpl(UUID id, String name, DataMiningReportDTO report) {
        this.id = id;
        this.name = name;
        this.report = report;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DataMiningReportDTO getReport() {
        return this.report;
    }
    
    public void setReport(DataMiningReportDTO report) {
        this.report = report;
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
        StoredDataMiningReportDTOImpl other = (StoredDataMiningReportDTOImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
