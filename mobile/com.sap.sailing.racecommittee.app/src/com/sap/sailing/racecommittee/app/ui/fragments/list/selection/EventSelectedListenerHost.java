package com.sap.sailing.racecommittee.app.ui.fragments.list.selection;

import com.sap.sailing.domain.base.Event;

public interface EventSelectedListenerHost {
	public ItemSelectedListener<Event> getEventSelectionListener();
}
