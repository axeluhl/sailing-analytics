package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.list.CourseAreaListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.list.EventListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.list.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.list.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.list.selection.ItemSelectedListener;

public class LoginActivity extends TwoPaneActivity implements EventSelectedListenerHost, CourseAreaSelectedListenerHost  {
	
	private final static String TAG = LoginActivity.class.getName();
	
	private enum LoginType {
		OFFICER, VIEWER;
	}
	
	private static final LoginType defaultLoginType = LoginType.OFFICER;
	
	private static final int DIALOG_LOGIN_TYPE = 1;
	
	private CharSequence[] loginTypeDescriptions;
	private LoginType selectedLoginType = defaultLoginType;
	private CourseArea selectedCourse = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.login_view);
        setProgressBarIndeterminateVisibility(false);      
        
        loginTypeDescriptions = new CharSequence[2];
		loginTypeDescriptions[0] = getString(R.string.login_type_officer);
		loginTypeDescriptions[1] = getString(R.string.login_type_viewer);
        
        // on first create add event list fragment
        if (savedInstanceState == null) {
        	addEventListFragment();
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	try {
    		this.dismissDialog(DIALOG_LOGIN_TYPE);
    	} catch (IllegalArgumentException e) {
    		// occurs when dialog was never shown
    	}
    }

    protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOGIN_TYPE:
			return createLoginTypeDialog();
		default:
			return null;
		}
	};
	
	private Dialog createLoginTypeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose view mode");
		builder.setIcon(R.drawable.ic_menu_login);
		builder.setSingleChoiceItems(loginTypeDescriptions, 0, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					selectedLoginType = LoginType.OFFICER;
					break;
				case 1:
					selectedLoginType = LoginType.VIEWER;
					break;
				default:
					throw new IllegalStateException("Unknown login type selected.");
				}
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				selectedLoginType = defaultLoginType;
				ExLog.i(ExLog.LOGIN_BUTTON_NEGATIVE, String.valueOf(selectedLoginType), getBaseContext());
			}
		});
		builder.setPositiveButton("Login", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ExLog.i(ExLog.LOGIN_BUTTON_POSITIVE, String.valueOf(selectedLoginType), getBaseContext());
				startRaceActivity(selectedCourse);
			}
		});
		return builder.create();
	}

	private void addEventListFragment() {	
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.leftContainer, new EventListFragment());
		transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
		transaction.commit();
	}
	
	private void addCourseAreaListFragment(Serializable eventId) {
		Bundle args = new Bundle();
		args.putSerializable(AppConstants.EventIdTag, eventId);
		
		Fragment fragment = new CourseAreaListFragment();
		fragment.setArguments(args);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
		transaction.replace(R.id.rightContainer, fragment);
		transaction.commit();
		ExLog.i("LoginActivity", "CourseFragment created.");
	}
    
	private ItemSelectedListener<Event> eventSelectionListener = new ItemSelectedListener<Event>() {
	
		public void itemSelected(Fragment sender, Event event) {
			Serializable eventId = event.getId();
			ExLog.i(ExLog.EVENT_SELECTED, eventId.toString(), getBaseContext());
			showCourseAreaListFragment(eventId);
		}
	};

	public ItemSelectedListener<Event> getEventSelectionListener() {
		return eventSelectionListener;
	}
	
	private void showCourseAreaListFragment(Serializable eventId) {
		Toast.makeText(LoginActivity.this, eventId.toString(), Toast.LENGTH_LONG).show();
		getRightLayout().setVisibility(View.VISIBLE);
		addCourseAreaListFragment(eventId);
	}
	
	private ItemSelectedListener<CourseArea> courseAreaSelectionListener = new ItemSelectedListener<CourseArea>() {

		public void itemSelected(Fragment sender, CourseArea courseArea) {
			ExLog.i(TAG, "Starting view for " + courseArea.getName());
			ExLog.i(ExLog.COURSE_SELECTED, courseArea.getName(), getBaseContext());
			selectCourseArea(courseArea);
		}
	};

	public ItemSelectedListener<CourseArea> getCourseAreaSelectionListener() {
		return courseAreaSelectionListener;
	}
	
	private void selectCourseArea(CourseArea courseArea) {
		selectedCourse = courseArea;
		showDialog(DIALOG_LOGIN_TYPE);
	}
	
	private void startRaceActivity(CourseArea courseArea) {
		
		switch (selectedLoginType) {
		case OFFICER:
			ExLog.i(TAG, "Communication with backend is active.");
			AppConstants.setSendingActive(this, true);
			break;
		case VIEWER:
			ExLog.i(TAG, "Communication with backend is inactive.");
			AppConstants.setSendingActive(this, false);
			break;
		default:
			Toast.makeText(this, "Invalid login type. Ignoring.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (courseArea == null) {
			Toast.makeText(this, "The selected course was lost.", Toast.LENGTH_LONG).show();
			ExLog.e(TAG, "Course reference was not set - cannot start racing activity.");
			return;
		}
		
		Toast.makeText(this, courseArea.getId().toString(), Toast.LENGTH_LONG).show();
		Intent message = new Intent(this, RacingActivity.class);
		message.putExtra(AppConstants.COURSE_AREA_UUID_KEY, courseArea.getId());
		fadeActivity(message);
	}

}
