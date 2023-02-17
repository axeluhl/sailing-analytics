package com.sap.sse.datamining.shared.dto;

import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Renamable;

/**
 * Annotates a {@link DataMiningReportDTO} with a {@link UUID} and a name. Objects of this
 * type are managed in the repository of data mining reports in the user preferences. Equality
 * and hash code are decided solely based on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface StoredDataMiningReportDTO extends NamedWithUUID, Renamable {
    DataMiningReportDTO getReport();
}
