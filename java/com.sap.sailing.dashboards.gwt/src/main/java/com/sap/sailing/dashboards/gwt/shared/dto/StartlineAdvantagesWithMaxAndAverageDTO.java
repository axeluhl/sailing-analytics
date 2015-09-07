package com.sap.sailing.dashboards.gwt.shared.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesWithMaxAndAverageDTO extends AverageDTO implements IsSerializable {
    
    public double maximum;
    public List<StartLineAdvantageDTO> advantages;
    
    public StartlineAdvantagesWithMaxAndAverageDTO(){}
}
