package com.sap.sailing.racecommittee.app.activities;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.fragments.list.NamedListFragment.ItemSelectedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class LoginActivity extends TwoPaneActivity  {
	
	private final static String TAG ="LoginActivity";
	
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
		/*FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.leftContainer, new EventListFragment());
		transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
		transaction.commit();*/
	}
	
	/*private void addCourseListFragment(Serializable eventId) {
		Fragment fragment = new CourseAreaListFragment();
		Bundle args = new Bundle();
		args.putSerializable(AppConstants.EventIdTag, eventId);
		fragment.setArguments(args);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
		transaction.replace(R.id.rightContainer, fragment);
		transaction.commit();
		ExLog.i("LoginActivity", "CourseFragment created.");
	}*/
	
	public ItemSelectedListener<Event> getEventSelectionListener() {
		return eventSelectionListener;
	}
    
	private ItemSelectedListener<Event> eventSelectionListener = new ItemSelectedListener<Event>() {
	
		public void itemSelected(Fragment sender, Event event) {
			Serializable eventId = event.getId();
			ExLog.i(ExLog.EVENT_SELECTED, eventId.toString(), getBaseContext());
			showCourseListFragment(eventId);
		}
	};
	
	private void showCourseListFragment(Serializable eventId) {
		Toast.makeText(LoginActivity.this, eventId.toString(), Toast.LENGTH_LONG).show();
		getRightLayout().setVisibility(View.VISIBLE);
		/*addCourseListFragment(eventId);*/
	}
	
	public ItemSelectedListener<CourseArea> getCourseSelectionListener() {
		return courseSelectionListener;
	}
	
	private ItemSelectedListener<CourseArea> courseSelectionListener = new ItemSelectedListener<CourseArea>() {

		public void itemSelected(Fragment sender, CourseArea course) {
			ExLog.i(TAG, "Starting view for " + course.getName());
			ExLog.i(ExLog.COURSE_SELECTED, course.getName(), getBaseContext());
			selectCourse(course);
		}
	};
	
	private void selectCourse(CourseArea course) {
		selectedCourse = course;
		showDialog(DIALOG_LOGIN_TYPE);
	}
	
	private void startRaceActivity(CourseArea course) {
		
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
		
		if (course == null) {
			Toast.makeText(this, "The selected course was lost.", Toast.LENGTH_LONG).show();
			ExLog.e(TAG, "Course reference was not set - cannot start racing activity.");
			return;
		}
		
		Toast.makeText(this, "Course " + course.getName(), Toast.LENGTH_LONG).show();
		/*Intent message = new Intent(this, RacingActivity.class);
		message.putExtra(AppConstants.COURSE_AREA_UUID_KEY, course.getId());
		fadeActivity(message);*/
	}

}
