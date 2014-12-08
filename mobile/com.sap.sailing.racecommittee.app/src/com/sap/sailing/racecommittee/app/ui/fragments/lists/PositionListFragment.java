package com.sap.sailing.racecommittee.app.ui.fragments.lists;


import java.util.ArrayList;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoginDialog.LoginType;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;


public class PositionListFragment extends LoggableListFragment{
	
    private LoginType selectedLoginType;
    private RaceLogEventAuthor author;
    private AppPreferences preferences;
	
    private PositionSelectedListenerHost host;
    
	@Override
	public void onResume() {
		
		final ArrayList<String> values = new ArrayList<String>();
		values.add(getString(R.string.login_type_officer_on_start_vessel));
		values.add(getString(R.string.login_type_officer_on_finish_vessel));
		values.add(getString(R.string.login_type_shore_control));
		values.add(getString(R.string.login_type_viewer));
		
		this.preferences = AppPreferences.on(getActivity().getApplicationContext());
		
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity().getBaseContext(), R.layout.login_list_item, R.id.txt_list_item, values);
		setListAdapter(adapter);
		
		if ( getActivity() instanceof PositionSelectedListenerHost ){
			this.host = (PositionSelectedListenerHost) getActivity();
		}
		
		super.onResume();
	}

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		//ExLog.i(getActivity(),position, "PositionListFragment");
		
	    switch (position) {
        // see loginTypeDescriptions for the indices of the login types
	        case 0:
	            selectedLoginType = LoginType.OFFICER;
	            preferences.setSendingActive(true);
	            author = new RaceLogEventAuthorImpl("Race Officer on Start Vessel", 0);
	            break;
	        case 1:
	            selectedLoginType = LoginType.OFFICER;
	            preferences.setSendingActive(true);
	            author = new RaceLogEventAuthorImpl("Race Officer on Finish Vessel", 1);
	            break;
	        case 2:
	            selectedLoginType = LoginType.OFFICER;
	            preferences.setSendingActive(true);
	            author = new RaceLogEventAuthorImpl("Shore Control", 2);
	            break;
	        case 3:
	            selectedLoginType = LoginType.VIEWER;
	            preferences.setSendingActive(false);
	            author = new RaceLogEventAuthorImpl("Viewer", 3);
	            break;
	        default:
	            selectedLoginType = LoginType.NONE;
	            break;
        }
	    
	    host.onPositionSelected(selectedLoginType);
	}
	
	
	
	
	protected String getHeaderText() {
        return getString(R.string.label_login_position);
    }
    

}