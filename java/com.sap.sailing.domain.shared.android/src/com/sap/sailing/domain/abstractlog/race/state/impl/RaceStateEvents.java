package com.sap.sailing.domain.abstractlog.race.state.impl;

public enum RaceStateEvents {
    UNKNOWN,
    START,
    INDIVIDUAL_RECALL_TIMEOUT,
    
    RRS26_CLASS_UP,
    RRS26_STARTMODE_UP,
    RRS26_STARTMODE_DOWN,

    SWC_CLASS_AND_STARTMODE_UP,
    SWC_CLASS_AND_STARTMODE_DOWN,
    SWC_FIVE_UP,
    SWC_FOUR_UP,
    SWC_THREE_UP,
    SWC_TWO_UP,
    SWC_ONE_UP,
    SWC_GREEN_UP,

    ESS_AP_DOWN,
    ESS_THREE_UP,
    ESS_TWO_UP,
    ESS_ONE_UP,
    
    GATE_CLASS_OVER_GOLF_UP,
    GATE_PAPA_UP,
    GATE_PAPA_DOWN,
    GATE_GOLF_DOWM, 
    GATE_SHUTDOWN
}
