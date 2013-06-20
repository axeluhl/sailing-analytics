package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class RegattaCreationParametersDTO implements Serializable {
    private static final long serialVersionUID = 6852529475564779063L;
    private LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters;

    RegattaCreationParametersDTO() {}
    
    public RegattaCreationParametersDTO(LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters) {
        super();
        this.seriesCreationParameters = seriesCreationParameters;
    }

    public LinkedHashMap<String, SeriesCreationParametersDTO> getSeriesCreationParameters() {
        return seriesCreationParameters;
    }
}
