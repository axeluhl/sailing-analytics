package com.sap.sailing.android.tracking.app.ui.fragments;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.sap.sailing.android.shared.data.BaseCheckinData;
import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperError;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperFailureListener;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.adapter.RegattaAdapter;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.ui.activities.BuoyActivity;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.ui.activities.StartActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.CheckinHelper;
import com.sap.sailing.android.tracking.app.utils.CheckinManager;
import com.sap.sailing.android.tracking.app.utils.CheckoutHelper;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.tracking.app.valueobjects.MarkCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.MarkInfo;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;

public class HomeFragment extends AbstractHomeFragment implements LoaderCallbacks<Cursor> {

    private final static String TAG = HomeFragment.class.getName();

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        prefs = new AppPreferences(getActivity());

        ListView listView = (ListView) view.findViewById(R.id.listRegatta);
        if (listView != null) {
            listView.addHeaderView(inflater.inflate(R.layout.regatta_listview_header, null));

            adapter = new RegattaAdapter(getActivity(), R.layout.ragatta_listview_row, null, 0);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new ItemClickListener());
            listView.setOnItemLongClickListener(new LongItemClickListener());
        }

        getLoaderManager().initLoader(REGATTA_LOADER, null, this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(REGATTA_LOADER, null, this);

        String lastQRCode = prefs.getLastScannedQRCode();
        if (lastQRCode != null) {
            handleQRCode(lastQRCode);
        }
    }

    /**
     * Perform a checkin request and launch RegattaAcitivity afterwards
     */
    private void checkInWithAPIAndDisplayTrackingActivity(CheckinData checkinData) {
        if (checkinData instanceof CompetitorCheckinData) {
            storeCompetitorCheckinData((CompetitorCheckinData) checkinData);
        } else if (checkinData instanceof MarkCheckinData) {
            storeMarkCheckinData((MarkCheckinData) checkinData);
        }
        performAPICheckin(checkinData);
    }

    private void storeCompetitorCheckinData(CompetitorCheckinData checkinData) {
        if (DatabaseHelper.getInstance().eventLeaderboardCompetitorCombinationAvailable(getActivity(), checkinData.checkinDigest)) {
            try {
                DatabaseHelper.getInstance().storeCompetitorCheckinRow(getActivity(), checkinData.getEvent(), checkinData.getCompetitor(), checkinData
                    .getLeaderboard(), checkinData.getCheckinUrl());
                adapter.notifyDataSetChanged();
            } catch (GeneralDatabaseHelperException e) {
                ExLog.e(getActivity(), TAG, "Batch insert failed: " + e.getMessage());
                ((StartActivity) getActivity()).displayDatabaseError();
                return;
            }

            if (BuildConfig.DEBUG) {
                ExLog.i(getActivity(), TAG, "Batch-insert of checkinData completed.");
            }
        } else {
            ExLog.w(getActivity(), TAG, "Combination of eventId, leaderboardName and competitorId already exists!");
            Toast.makeText(getActivity(), getString(R.string.info_already_checked_in_this_qr_code), Toast.LENGTH_LONG).show();
        }
    }

    private void storeMarkCheckinData(MarkCheckinData checkinData) {
        if (DatabaseHelper.getInstance().eventLeaderboardMarkCombinationAvailable(getActivity(), checkinData.checkinDigest)) {
            try {
                DatabaseHelper.getInstance().storeMarkCheckinRow(getActivity(), checkinData.getEvent(), checkinData.getMark(), checkinData
                    .getLeaderboard(), checkinData.getCheckinUrl());
                adapter.notifyDataSetChanged();
            } catch (GeneralDatabaseHelperException e) {
                ExLog.e(getActivity(), TAG, "Batch insert failed: " + e.getMessage());
                ((StartActivity) getActivity()).displayDatabaseError();
                return;
            }

            if (BuildConfig.DEBUG) {
                ExLog.i(getActivity(), TAG, "Batch-insert of checkinData completed.");
            }
        } else {
            ExLog.w(getActivity(), TAG, "Combination of eventId, leaderboardName and markID already exists!");
            Toast.makeText(getActivity(), getString(R.string.info_already_checked_in_this_qr_code), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checkin with API.
     *
     * @param checkinData
     */
    private void performAPICheckin(CheckinData checkinData) {
        Date date = new Date();
        StartActivity startActivity = (StartActivity) getActivity();
        startActivity.showProgressDialog(R.string.please_wait, R.string.checking_in);
        try {
            JSONObject requestObject = null;
            if (checkinData instanceof CompetitorCheckinData) {
                CompetitorCheckinData competitorCheckinData = (CompetitorCheckinData) checkinData;
                requestObject = CheckinHelper
                    .getCompetitorCheckinJson(competitorCheckinData.competitorId, competitorCheckinData.deviceUid, "TODO push device ID!!", date
                        .getTime());
            } else if (checkinData instanceof MarkCheckinData) {
                MarkCheckinData markCheckinData = (MarkCheckinData) checkinData;
                requestObject = CheckinHelper
                    .getMarkCheckinJson(markCheckinData.getMark().getId().toString(), markCheckinData.deviceUid, "TODO push device ID!!", date
                        .getTime());
            }
            HttpJsonPostRequest request = new HttpJsonPostRequest(getActivity(), new URL(checkinData.checkinURL), requestObject.toString());
            NetworkHelper.getInstance(getActivity())
                .executeHttpJsonRequestAsync(request, new CheckinListener(checkinData.checkinDigest, checkinData.getCheckinType()), new CheckinErrorListener(checkinData.checkinDigest));
        } catch (JSONException|NullPointerException e) {
            ExLog.e(getActivity(), TAG, "Failed to generate checkin JSON: " + e.getMessage());
            displayAPIErrorRecommendRetry();
        } catch (MalformedURLException e) {
            ExLog.e(getActivity(), TAG, "Failed to perform checkin, MalformedURLException: " + e.getMessage());
            displayAPIErrorRecommendRetry();
        }
    }

    @Override
    public void handleScannedOrUrlMatchedUri(Uri uri) {
        String uriString = uri.toString();
        CheckinManager manager = new CheckinManager(uriString, (StartActivity) getActivity(), false);
        manager.callServerAndGenerateCheckinData();
    }

    /**
     * Display a confirmation-dialog in which the user confirms his full name and sail-id.
     */
    @Override
    public void displayUserConfirmationScreen(final BaseCheckinData data) {
        if (data instanceof CompetitorCheckinData) {
            final CompetitorCheckinData checkinData = (CompetitorCheckinData) data;
            String message1 = getString(R.string.confirm_data_hello_name).replace("{full_name}", checkinData.competitorName);
            String message2 = getString(R.string.confirm_data_you_are_signed_in_as_sail_id).replace("{sail_id}", checkinData.competitorSailId);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(message1 + "\n\n" + message2);
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.confirm_data_is_correct), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearScannedQRCodeInPrefs();
                    checkInWithAPIAndDisplayTrackingActivity(checkinData);
                }
            }).setNegativeButton(R.string.decline_data_is_incorrect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearScannedQRCodeInPrefs();
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (data instanceof MarkCheckinData) {
            MarkCheckinData checkinData = (MarkCheckinData) data;
            clearScannedQRCodeInPrefs();
            checkInWithAPIAndDisplayTrackingActivity(checkinData);
        }
    }

    /**
     * Start regatta activity.
     *
     * @param checkinDigest
     */
    private void startRegatta(String checkinDigest, int type) {
        Intent intent = new Intent();
        if (type == CheckinUrlInfo.TYPE_COMPETITOR) {
            intent.setClass(getActivity(), RegattaActivity.class);
        } else if (type == CheckinUrlInfo.TYPE_MARK) {
            intent.setClass(getActivity(), BuoyActivity.class);
        }
        intent.putExtra(getString(R.string.checkin_digest), checkinDigest);
        getActivity().startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case REGATTA_LOADER:
                String[] projection = new String[] { AnalyticsContract.PATH_EVENT + "." + AnalyticsContract.Event.EVENT_CHECKIN_DIGEST,
                    AnalyticsContract.PATH_EVENT + "." + AnalyticsContract.Event._ID,
                    AnalyticsContract.PATH_EVENT + "." + AnalyticsContract.Event.EVENT_NAME,
                    AnalyticsContract.PATH_EVENT + "." + AnalyticsContract.Event.EVENT_SERVER,
                    AnalyticsContract.PATH_LEADERBOARD + "." + AnalyticsContract.Leaderboard.LEADERBOARD_NAME,
                    AnalyticsContract.PATH_COMPETITOR + "." + AnalyticsContract.Competitor.COMPETITOR_DISPLAY_NAME,
                    AnalyticsContract.PATH_MARK + "." + AnalyticsContract.Mark.MARK_NAME,
                    AnalyticsContract.PATH_CHECKIN + "." + AnalyticsContract.Checkin.CHECKIN_TYPE};
                return new CursorLoader(getActivity(), AnalyticsContract.LeaderboardsEventsCompetitorsMarksJoined.CONTENT_URI, projection, null, null, null);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case REGATTA_LOADER:
                adapter.changeCursor(cursor);
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case REGATTA_LOADER:
                adapter.changeCursor(null);
                break;

            default:
                break;
        }
    }

    private void reloadList() {
        getLoaderManager().restartLoader(REGATTA_LOADER, null, this);
    }

    private boolean showDeleteConfirmationDialog(int position) {
        // -1, because there's a header row
        Cursor cursor = (Cursor) adapter.getItem(position - 1);
        final String checkinDigest = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Event.EVENT_CHECKIN_DIGEST));
        final int type = cursor.getInt(cursor.getColumnIndex(AnalyticsContract.Checkin.CHECKIN_TYPE));
        DatabaseHelper.getInstance().getEventInfo(getActivity(), checkinDigest);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Base_Theme_AppCompat_Dialog_Alert);
        builder.setMessage(getString(R.string.confirm_delete_checkin));
        builder.setCancelable(true);
        builder.setNegativeButton(getString(R.string.no), null);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRegatta(checkinDigest, type);
            }
        });
        builder.show();

        return true;
    }

    private void deleteRegatta(final String checkinDigest, final int type) {
        NetworkHelperSuccessListener successListener = new NetworkHelperSuccessListener() {
            @Override
            public void performAction(JSONObject response) {
                StartActivity startActivity = (StartActivity) getActivity();
                startActivity.dismissProgressDialog();
                DatabaseHelper.getInstance().deleteRegattaFromDatabase(getActivity(), checkinDigest);
                reloadList();
            }
        };
        NetworkHelperFailureListener failureListener = new NetworkHelperFailureListener() {
            @Override
            public void performAction(NetworkHelperError e) {
                StartActivity startActivity = (StartActivity) getActivity();
                startActivity.dismissProgressDialog();
                startActivity.showErrorPopup(R.string.error,
                    R.string.error_could_not_complete_operation_on_server_try_again);
            }
        };
        CheckoutHelper checkoutHelper = new CheckoutHelper();
        LeaderboardInfo leaderboardInfo = DatabaseHelper.getInstance().getLeaderboard(getActivity(), checkinDigest);
        EventInfo eventInfo = DatabaseHelper.getInstance().getEventInfo(getActivity(), checkinDigest);
        if (type == CheckinUrlInfo.TYPE_COMPETITOR) {
            CompetitorInfo competitorInfo = DatabaseHelper.getInstance().getCompetitor(getActivity(), checkinDigest);
            checkoutHelper.checkoutCompetitor((StartActivity) getActivity(), leaderboardInfo.name, eventInfo.server, competitorInfo.id,
                successListener, failureListener);
        } else if (type == CheckinUrlInfo.TYPE_MARK) {
            MarkInfo markInfo = DatabaseHelper.getInstance().getMarkInfo(getActivity(), checkinDigest);
            checkoutHelper.checkoutMark((StartActivity) getActivity(), leaderboardInfo.name, eventInfo.server, markInfo.markId,
                successListener, failureListener);
        }
    }

    private class ItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position < 1) // tapped header
            {
                return;
            }

            // -1, because there's a header row
            Cursor cursor = (Cursor) adapter.getItem(position - 1);

            String checkinDigest = cursor.getString(cursor.getColumnIndex(AnalyticsContract.Event.EVENT_CHECKIN_DIGEST));
            int type = cursor.getInt(cursor.getColumnIndex(AnalyticsContract.Checkin.CHECKIN_TYPE));
            startRegatta(checkinDigest, type);
        }
    }

    private class LongItemClickListener implements OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position < 1) // tapped header
            {
                return false;
            }
            return showDeleteConfirmationDialog(position);
        }

    }

    private class CheckinListener implements NetworkHelperSuccessListener {

        public String checkinDigest;
        public int type;

        public CheckinListener(String checkinDigest, int type) {
            this.checkinDigest = checkinDigest;
            this.type = type;
        }

        @Override
        public void performAction(JSONObject response) {
            StartActivity startActivity = (StartActivity) getActivity();
            startActivity.dismissProgressDialog();
            startRegatta(checkinDigest, type);
        }
    }

    private class CheckinErrorListener implements NetworkHelperFailureListener {

        public String checkinDigest;

        public CheckinErrorListener(String checkinDigest) {
            this.checkinDigest = checkinDigest;
        }

        @Override
        public void performAction(NetworkHelperError e) {
            if (e.getMessage() != null) {
                ExLog.e(getActivity(), TAG, e.getMessage().toString());
            } else {
                ExLog.e(getActivity(), TAG, "Unknown Error");
            }

            StartActivity startActivity = (StartActivity) getActivity();
            startActivity.dismissProgressDialog();
            startActivity.showErrorPopup(R.string.error, R.string.error_could_not_complete_operation_on_server_try_again);

            DatabaseHelper.getInstance().deleteRegattaFromDatabase(getActivity(), checkinDigest);
            Toast.makeText(getActivity(), getString(R.string.error_while_receiving_server_data), Toast.LENGTH_LONG).show();
        }
    }

}
