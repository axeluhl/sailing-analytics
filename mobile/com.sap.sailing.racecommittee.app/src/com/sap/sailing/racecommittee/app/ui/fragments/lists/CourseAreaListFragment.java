package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;

import android.app.Activity;
import android.os.Bundle;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;

public class CourseAreaListFragment extends NamedListFragment<CourseArea>  {

	private Serializable parentEventId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.parentEventId = getArguments().getSerializable(AppConstants.EventIdTag);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadItems();
	}
	
	@Override
	protected ItemSelectedListener<CourseArea> attachListener(Activity activity) {
		if (activity instanceof EventSelectedListenerHost) { 
			CourseAreaSelectedListenerHost listener = (CourseAreaSelectedListenerHost) activity;
			return listener.getCourseAreaSelectionListener();
		}
		
		throw new IllegalStateException(String.format(
				"%s cannot be attached to a instance of %s", 
				CourseAreaListFragment.class.getName(),
				activity.getClass().getName()));
	}

	@Override
	protected String getHeaderText() {
		return getString(R.string.label_login_course_area);
	}

	@Override
	protected void loadItems(ReadonlyDataManager manager) {
		manager.loadCourseAreas(parentEventId, this);
	}

}
