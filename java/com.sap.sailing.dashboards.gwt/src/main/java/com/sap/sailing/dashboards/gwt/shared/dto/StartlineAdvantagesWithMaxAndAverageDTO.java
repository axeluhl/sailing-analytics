package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.List;

import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesWithMaxAndAverageDTO extends AverageDTO implements Result {
    
    public Double maximum;
    public List<StartLineAdvantageDTO> advantages;
    
    public StartlineAdvantagesWithMaxAndAverageDTO(){}
}
