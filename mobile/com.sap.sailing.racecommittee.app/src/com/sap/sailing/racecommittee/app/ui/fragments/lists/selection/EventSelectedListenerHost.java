package com.sap.sailing.racecommittee.app.ui.fragments.lists.selection;

import com.sap.sailing.domain.base.EventBase;

public interface EventSelectedListenerHost {
    public ItemSelectedListener<EventBase> getEventSelectionListener();
}
