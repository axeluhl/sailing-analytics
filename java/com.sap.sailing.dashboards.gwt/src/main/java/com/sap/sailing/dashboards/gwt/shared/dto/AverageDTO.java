package com.sap.sailing.dashboards.gwt.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class AverageDTO implements IsSerializable {

    public double average;
    public double collectingTimeFrameInMilliseconds;
    
    public AverageDTO(){}
}
