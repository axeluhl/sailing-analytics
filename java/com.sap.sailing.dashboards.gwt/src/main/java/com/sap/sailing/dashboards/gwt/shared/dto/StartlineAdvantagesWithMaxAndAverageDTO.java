package com.sap.sailing.dashboards.gwt.shared.dto;

import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesWithMaxAndAverageDTO extends AverageDTO implements Result {
    
    public Double maximum;
    public Number [][] distanceToRCBoatToStartlineAdvantage;
    public Number [][] distanceToRCBoatToConfidence;
    
    public StartlineAdvantagesWithMaxAndAverageDTO(){}
}
