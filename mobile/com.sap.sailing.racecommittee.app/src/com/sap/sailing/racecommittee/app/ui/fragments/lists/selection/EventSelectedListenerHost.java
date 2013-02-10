package com.sap.sailing.racecommittee.app.ui.fragments.lists.selection;

import com.sap.sailing.domain.base.Event;

public interface EventSelectedListenerHost {
	public ItemSelectedListener<Event> getEventSelectionListener();
}
