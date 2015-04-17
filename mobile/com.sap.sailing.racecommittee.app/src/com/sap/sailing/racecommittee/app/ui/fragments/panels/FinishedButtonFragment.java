package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.*;

public class FinishedButtonFragment extends BasePanelFragment {

    private final static String TAG = FinishedButtonFragment.class.getName();

    private IntentReceiver mReceiver;
    private RelativeLayout mRecord;
    private View mRecordLock;
    private RelativeLayout mPhoto;
    private View mPhotoLock;
    private RelativeLayout mList;
    private View mListLock;

    public FinishedButtonFragment() {
        mReceiver = new IntentReceiver();
    }

    public static FinishedButtonFragment newInstance(Bundle args) {
        FinishedButtonFragment fragment = new FinishedButtonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_left, container, false);

        mRecord = ViewHolder.get(layout, R.id.record_button);
        if (mRecord != null) {
            mRecord.setOnClickListener(new RecordClick());
        }
        mRecordLock = ViewHolder.get(layout, R.id.voice_lock);

        mPhoto = ViewHolder.get(layout, R.id.photo_button);
        if (mPhoto != null) {
            mPhoto.setOnClickListener(new PhotoClick());
        }
        mPhotoLock = ViewHolder.get(layout, R.id.photo_lock);
        if (!isCameraAvailable(getActivity())) {
            mPhotoLock.setVisibility(View.VISIBLE);
        }

        mList = ViewHolder.get(layout, R.id.list_button);
        if (mList != null) {
            mList.setOnClickListener(new ListClick());
        }
        mListLock = ViewHolder.get(layout, R.id.list_lock);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void uncheckMarker(View view) {
        if (view != null) {
            if (!view.equals(mRecord)) {
                resetFragment(null, R.id.race_frame, StartProcedureFragment.class);
                setMarkerLevel(mRecord, R.id.record_marker, 0);
            }

            if (!view.equals(mPhoto)) {
                resetFragment(null, R.id.race_frame, StartModeFragment.class);
                setMarkerLevel(mPhoto, R.id.photo_marker, 0);
            }

            if (!view.equals(mList)) {
                resetFragment(null, R.id.race_frame, CourseFragment.class);
                setMarkerLevel(mList, R.id.list_marker, 0);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static  boolean isCameraAvailable(Context context) {
        PackageManager manager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        } else {
            return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA) || manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        }
    }

    private class RecordClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mRecordLock == null || mRecordLock.getVisibility() == View.GONE) {
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_REPLAY);
                switch (toggleMarker(v, R.id.record_marker)) {
                    case 0:
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                        break;

                    case 1:
                        // TODO
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                        break;
                }
            }
        }
    }

    private class PhotoClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mPhotoLock == null || mPhotoLock.getVisibility() == View.GONE) {
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_PHOTOS);
                switch (toggleMarker(v, R.id.photo_marker)) {
                    case 0:
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                        break;

                    case 1:
                        replaceFragment(PhotoListFragment.newInstance(getRecentArguments()), R.id.finished_content);
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                        break;
                }
            }
        }
    }

    private class ListClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mListLock == null || mListLock.getVisibility() == View.GONE) {
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_LIST);
                switch (toggleMarker(v, R.id.list_marker)) {
                    case 0:
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                        break;

                    case 1:
                        replaceFragment(PositioningFragment.newInstance(getRecentArguments()), R.id.finished_content);
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                        break;
                }
            }
        }
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.INTENT_ACTION_CLEAR_TOGGLE.equals(action)) {
                uncheckMarker(new View(context));
            }

            if (AppConstants.INTENT_ACTION_TOGGLE.equals(action)) {
                if (intent.getExtras() != null) {
                    String data = intent.getExtras().getString(AppConstants.INTENT_ACTION_EXTRA);
                    if (AppConstants.INTENT_ACTION_TOGGLE_REPLAY.equals(data)) {
                        uncheckMarker(mRecord);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_PHOTOS.equals(data)) {
                        uncheckMarker(mPhoto);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_LIST.equals(data)) {
                        uncheckMarker(mList);
                    } else {
                        uncheckMarker(new View(context));
                    }
                }
            }
        }
    }
}
