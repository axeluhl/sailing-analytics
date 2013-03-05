package com.sap.sailing.racecommittee.app.ui.fragments.lists.selection;

import com.sap.sailing.domain.base.EventData;

public interface EventSelectedListenerHost {
	public ItemSelectedListener<EventData> getEventSelectionListener();
}
