package com.sap.sailing.dashboards.gwt.shared.dto;

import com.sap.sse.gwt.dispatch.client.commands.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class AverageDTO implements Result {

    public Double average;
    public Double collectingTimeFrameInMilliseconds;
    
    public AverageDTO(){}
}
