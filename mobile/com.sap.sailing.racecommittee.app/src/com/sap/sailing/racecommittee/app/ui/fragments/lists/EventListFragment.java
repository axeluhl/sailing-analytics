package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import android.app.Activity;

import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

public class EventListFragment extends NamedListFragment<EventData> {

	@Override
	public void onResume() {
		super.onResume();
		loadItems();
	}
	
	@Override
	protected ItemSelectedListener<EventData> attachListener(Activity activity) {
		if (activity instanceof EventSelectedListenerHost) { 
			EventSelectedListenerHost listener = (EventSelectedListenerHost) activity;
			return listener.getEventSelectionListener();
		}
		
		throw new IllegalStateException(String.format(
				"%s cannot be attached to a instance of %s", 
				EventListFragment.class.getName(),
				activity.getClass().getName()));
	}

	@Override
	protected String getHeaderText() {
		return getString(R.string.label_login_events);
	}

	@Override
	protected void loadItems(ReadonlyDataManager manager) {
		manager.loadEvents(this);
	}

}
