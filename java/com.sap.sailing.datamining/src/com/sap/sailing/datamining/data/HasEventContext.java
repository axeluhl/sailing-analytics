package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Event;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasEventContext {
    
    @SideEffectFreeValue(messageKey="Event")
    public Event getEvent();

}
