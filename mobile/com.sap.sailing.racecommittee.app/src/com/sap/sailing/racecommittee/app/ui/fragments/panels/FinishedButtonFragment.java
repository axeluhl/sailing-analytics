package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.PhotoListFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.TrackingListFragment;

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
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FinishedButtonFragment extends BasePanelFragment {

    private final static String TAG = FinishedButtonFragment.class.getName();

    private IntentReceiver mReceiver;
    private RelativeLayout mRecord;
    private View mRecordLock;
    private RelativeLayout mPhoto;
    private View mPhotoLock;
    private RelativeLayout mList;
    private View mListLock;
    private ImageView mWarning;

    public FinishedButtonFragment() {
        mReceiver = new IntentReceiver();
    }

    public static FinishedButtonFragment newInstance(Bundle args) {
        FinishedButtonFragment fragment = new FinishedButtonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isCameraAvailable(Context context) {
        PackageManager manager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        } else {
            return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                    || manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_buttons, container, false);

        mRecord = ViewHelper.get(layout, R.id.record_button);
        if (mRecord != null) {
            mRecord.setOnClickListener(new RecordClick());
        }
        mRecordLock = ViewHelper.get(layout, R.id.voice_lock);

        mPhoto = ViewHelper.get(layout, R.id.photo_button);
        if (mPhoto != null) {
            mPhoto.setOnClickListener(new PhotoClick());
        }
        mPhotoLock = ViewHelper.get(layout, R.id.photo_lock);
        if (!isCameraAvailable(getActivity())) {
            mPhotoLock.setVisibility(View.VISIBLE);
        }

        mList = ViewHelper.get(layout, R.id.list_button);
        if (mList != null) {
            mList.setOnClickListener(new ListClick());
        }
        mListLock = ViewHelper.get(layout, R.id.list_lock);

        mWarning = ViewHelper.get(layout, R.id.panel_additional_image);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        filter.addAction(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);

        if (!preferences.getRacingProcedureIsResultEntryEnabled(getRaceState().getRacingProcedure().getType())) {
            mList.setVisibility(View.GONE);
        } else {
            CompetitorResults results = getRaceState().getConfirmedFinishPositioningList();
            if (results != null) {
                mWarning.setVisibility(results.hasConflicts() ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void uncheckMarker(View view) {
        if (isAdded() && view != null) {
            if (!view.equals(mRecord)) {
                // TODO
                setMarkerLevel(mRecord, R.id.record_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mPhoto)) {
                resetFragment(null, getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false),
                        PhotoListFragment.class);
                setMarkerLevel(mPhoto, R.id.photo_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mList)) {
                resetFragment(null, getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false),
                        TrackingListFragment.class);
                setMarkerLevel(mList, R.id.list_marker, LEVEL_NORMAL);
            }
        }
    }

    private class RecordClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mRecordLock == null || mRecordLock.getVisibility() == View.GONE) {
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                        AppConstants.INTENT_ACTION_TOGGLE_REPLAY);
                switch (toggleMarker(v, R.id.record_marker)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                    break;

                case LEVEL_TOGGLED:
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
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                        AppConstants.INTENT_ACTION_TOGGLE_PHOTOS);
                switch (toggleMarker(v, R.id.photo_marker)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(PhotoListFragment.newInstance(getRecentArguments()),
                            getFrameId(getActivity(), R.id.finished_edit, R.id.finished_content, true));
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
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                        AppConstants.INTENT_ACTION_TOGGLE_LIST);
                switch (toggleMarker(v, R.id.list_marker)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(TrackingListFragment.newInstance(getRecentArguments(), 0),
                            getFrameId(getActivity(), R.id.finished_edit, R.id.finished_content, true));
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
                    switch (data) {
                    case AppConstants.INTENT_ACTION_TOGGLE_REPLAY:
                        uncheckMarker(mRecord);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_PHOTOS:
                        uncheckMarker(mPhoto);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_LIST:
                        uncheckMarker(mList);
                        break;
                    default:
                        uncheckMarker(new View(context));
                        break;
                    }
                }
            }
        }
    }
}
