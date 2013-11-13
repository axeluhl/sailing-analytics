package com.sap.sailing.domain.racelog.state.impl;

public enum RaceStateEvents {
    UNKNOWN,
    START,
    INDIVIDUAL_RECALL_TIMEOUT,
    
    RRS26_CLASS_UP,
    RRS26_STARTMODE_UP,
    RRS26_STARTMODE_DOWN,
    
    ESS_AP_DOWN,
    ESS_THREE_UP,
    ESS_TWO_UP,
    ESS_ONE_UP
}
