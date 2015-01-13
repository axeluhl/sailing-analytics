package com.sap.sailing.domain.abstractlog.race.state;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.base.Timed;

public interface RaceStateEvent extends Timed, Serializable {
    
    RaceStateEvents getEventName();

}
