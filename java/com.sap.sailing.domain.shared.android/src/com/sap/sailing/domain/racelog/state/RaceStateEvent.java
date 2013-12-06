package com.sap.sailing.domain.racelog.state;

import java.io.Serializable;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;

public interface RaceStateEvent extends Timed, Serializable {
    
    RaceStateEvents getEventName();

}
