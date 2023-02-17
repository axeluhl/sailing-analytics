package com.sap.sse.datamining.shared.impl.dto;

import java.util.UUID;

import com.sap.sse.common.impl.RenamableImpl;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningReportDTO;

public class StoredDataMiningReportDTOImpl extends RenamableImpl implements StoredDataMiningReportDTO {
    private static final long serialVersionUID = 9218620680326470175L;
    
    private UUID id;
    private DataMiningReportDTO report;

    @Deprecated // for GWT serialization only
    StoredDataMiningReportDTOImpl() {
        super(null);
    }

    public StoredDataMiningReportDTOImpl(UUID id, String name, DataMiningReportDTO report) {
        super(name);
        this.id = id;
        this.report = report;
    }

    @Override
    public UUID getId() {
        return this.id;
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
