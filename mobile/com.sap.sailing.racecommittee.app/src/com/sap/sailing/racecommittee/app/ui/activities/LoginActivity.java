package com.sap.sailing.racecommittee.app.ui.activities;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.UUID;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationIdentifierImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesDeviceConfigurationLoader;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoginDialog.LoginType;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.CourseAreaListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.EventListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.PositionListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.CourseAreaSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.EventSelectedListenerHost;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.ItemSelectedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.selection.PositionSelectedListenerHost;
import com.sap.sailing.racecommittee.app.utils.autoupdate.AutoUpdater;


public class LoginActivity extends BaseActivity implements EventSelectedListenerHost, CourseAreaSelectedListenerHost, PositionSelectedListenerHost {


	private final static String CourseAreaListFragmentTag = "CourseAreaListFragmentTag";
    private final static String AreaPositionListFragmentTag = "AreaPositionListFragmentTag";

    private final static String TAG = LoginActivity.class.getName();

    
    // oben links
    private ItemSelectedListener<EventBase> eventSelectionListener = new ItemSelectedListener<EventBase>() {

        public void itemSelected(Fragment sender, EventBase event) {
            final Serializable eventId = event.getId();
            ExLog.i(LoginActivity.this, LogEvent.EVENT_SELECTED, eventId.toString());
            preferences.setEventID(eventId);
            setupDataManager(eventId);
            showCourseAreaListFragment(eventId);
        }
    };
    
    // oben rechts
    private ItemSelectedListener<CourseArea> courseAreaSelectionListener = new ItemSelectedListener<CourseArea>() {

        public void itemSelected(Fragment sender, CourseArea courseArea) {
            ExLog.i(LoginActivity.this, TAG, "Starting view for " + courseArea.getName());
            ExLog.i(LoginActivity.this, LogEvent.COURSE_SELECTED, courseArea.getName());
            selectCourseArea(courseArea.getId());
        }
    };
    
    private PositionListFragment mPositionListFragment;
    
    private ProgressBar mProgressSpinner;


    private Serializable mSelectedEvent;
    
    private final int RQS_GooglePlayServices = 1;
 
    private UUID mSelectedCourseAreaUUID;
    public LoginActivity() {
//        mLoginDialog = new LoginDialog();
        mPositionListFragment = new PositionListFragment();
//        mSelectedCourseArea = null;
    }

    private void addAreaPositionListFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
        transaction.replace(R.id.login_view_bottom_container, mPositionListFragment, AreaPositionListFragmentTag);
        transaction.commitAllowingStateLoss();
        ExLog.i(this, "LoginActivity", "PositionFragment created.");
    }

    
    private void addCourseAreaListFragment(Serializable eventId) {
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.EventIdTag, eventId);
        mSelectedEvent = eventId;
        hideAreaPositionListFragment();
        Fragment fragment = new CourseAreaListFragment();
        fragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
        transaction.replace(R.id.login_view_right_container, fragment, CourseAreaListFragmentTag);
        transaction.commit();
        ExLog.i(this, "LoginActivity", "CourseFragment created.");
    }
    
    private void addEventListFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.login_view_left_container, new EventListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        transaction.commit();
    }
	public ItemSelectedListener<CourseArea> getCourseAreaSelectionListener() {
        return courseAreaSelectionListener;
    }

    public ItemSelectedListener<EventBase> getEventSelectionListener() {
        return eventSelectionListener;
    }

    private void hideAreaPositionListFragment(){
    	 FragmentTransaction transaction = getFragmentManager().beginTransaction();
         transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
         transaction.replace(R.id.login_view_bottom_container, new Fragment(), AreaPositionListFragmentTag);
         transaction.commitAllowingStateLoss();
         ExLog.i(this, "LoginActivity", "PositionFragment created.");
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // features must be requested before anything else
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_view);
        setProgressBarIndeterminateVisibility(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        }
        
        addEventListFragment();
        
        UUID courseUUID = preferences.getCourseUUID();
        if ( courseUUID != new UUID(0,0) ){
        	mSelectedCourseAreaUUID = courseUUID;
        	
        }
        
        Serializable eventId = preferences.getEventID();
        if ( eventId != null ){
        	mSelectedEvent = eventId;
//        	setupDataManager(eventId);
//            showCourseAreaListFragment(eventId);
        }
        
//        // on first create add event list fragment
//        if (savedInstanceState == null) {
//            ExLog.i(this, TAG, "Seems to be first start. Creating event fragment.");
//            addEventListFragment();
//        }
        
        // sets up the global configuration and adds it to the preferences
        setupDataManager(null);
        
        new AutoUpdater(this).notifyAfterUpdate();        
    }
    
    @Override
	public void onPositionSelected(LoginType type) {
		
        if (mSelectedCourseAreaUUID == null) {
            Toast.makeText(LoginActivity.this, "The selected course area was lost.", Toast.LENGTH_LONG).show();
            ExLog.e(LoginActivity.this, TAG,
                    "Course area reference was not set - cannot start racing activity.");
            return;
        }
        preferences.setLoginType(type);
        preferences.isSetUp(true);
        
        ExLog.i(LoginActivity.this, TAG,
                "mSelectedEvent : " + mSelectedEvent);
		Intent message = new Intent(LoginActivity.this, RacingActivity.class);
        message.putExtra(AppConstants.COURSE_AREA_UUID_KEY, mSelectedCourseAreaUUID );
        message.putExtra(AppConstants.EventIdTag, mSelectedEvent);
        fadeActivity(message);
	}

    @Override
    protected boolean onReset() {
        Fragment courseAreaFragment = getFragmentManager().findFragmentByTag(CourseAreaListFragmentTag);
        if (courseAreaFragment != null) {
            getFragmentManager().beginTransaction().remove(courseAreaFragment).commit();
        }
        recreate();
        return true;
    }




    
    @Override
    public void onResume() {
        super.onResume();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices).show();
        }
        
        if (  mSelectedEvent != null && preferences.isSetUp() ){
        	showCourseAreaListFragment(mSelectedEvent);
        	
        	
        	Intent message = new Intent(LoginActivity.this, RacingActivity.class);
            message.putExtra(AppConstants.COURSE_AREA_UUID_KEY, mSelectedCourseAreaUUID );
            message.putExtra(AppConstants.EventIdTag, mSelectedEvent);
            fadeActivity(message);
        } 
    }

    private void selectCourseArea(UUID courseAreaUUID) {
        //mSelectedCourseArea = courseArea;
        mSelectedCourseAreaUUID = courseAreaUUID;
        preferences.setCourseUUID(mSelectedCourseAreaUUID);
        showAreaPositionListFragment();
    }



    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        super.setSupportProgressBarIndeterminateVisibility(visible);

        if (mProgressSpinner != null) {
            if (visible) {
                mProgressSpinner.setVisibility(View.VISIBLE);
            } else {
                mProgressSpinner.setVisibility(View.GONE);
            }
        }
    }

    private void setupDataManager(Serializable eventId){
    	final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.loading_configuration));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    	
        ReadonlyDataManager dataManager = DataManager.create(LoginActivity.this);
        DeviceConfigurationIdentifier identifier = new DeviceConfigurationIdentifierImpl(AppPreferences.on(
                getApplicationContext()).getDeviceIdentifier());

        LoaderCallbacks<?> configurationLoader = dataManager.createConfigurationLoader(identifier,
                new LoadClient<DeviceConfiguration>() {

                    @Override
                    public void onLoadFailed(Exception reason) {
                        setProgressBarIndeterminateVisibility(false);
                        progressDialog.dismiss();

                        if (reason instanceof FileNotFoundException) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.loading_configuration_not_found), Toast.LENGTH_LONG).show();
                            ExLog.w(LoginActivity.this,
                                    TAG,
                                    String.format("There seems to be no configuration for this device: %s",
                                            reason.toString()));
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.loading_configuration_failed), Toast.LENGTH_LONG).show();
                            ExLog.ex(LoginActivity.this, TAG, reason);
                        }

                        
                    }

                    @Override
                    public void onLoadSucceded(DeviceConfiguration configuration, boolean isCached) {
                        setProgressBarIndeterminateVisibility(false);
                        progressDialog.dismiss();

                        // this is our 'global' configuration, let's store it in app preferences
                        PreferencesDeviceConfigurationLoader.wrap(configuration, preferences).store();

                        Toast.makeText(getApplicationContext(), getString(R.string.loading_configuration_succeded),
                                Toast.LENGTH_LONG).show();
                        //showCourseAreaListFragment(eventId);
                    }
                }); 
       // always reload the configuration...
        getLoaderManager().restartLoader(0, null, configurationLoader).forceLoad();;
    }
    
   
    
    private void showAreaPositionListFragment() {
        addAreaPositionListFragment();
    }


	private void showCourseAreaListFragment(Serializable eventId) {
        addCourseAreaListFragment(eventId);
    }

}
