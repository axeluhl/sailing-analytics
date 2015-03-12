package com.sap.sailing.android.tracking.app.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.shared.ui.adapters.RegattaAdapter;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.ui.activities.StartActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.CheckinHelper;
import com.sap.sailing.android.tracking.app.utils.CheckinManager;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperError;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperFailureListener;
import com.sap.sailing.android.shared.util.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class HomeFragment extends BaseFragment implements LoaderCallbacks<Cursor> {

    private final static String TAG = HomeFragment.class.getName();
    private final static int REGATTA_LOADER = 1;

    private AppPreferences prefs;

    private int requestCodeQRCode = 442;
    private RegattaAdapter adapter;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        prefs = new AppPreferences(getActivity());

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button scanButton = (Button) view.findViewById(R.id.scanQr);
        if (scanButton != null) {
            scanButton.setOnClickListener(new ClickListener());
        }

        Button noQrCodeButton = (Button) view.findViewById(R.id.noQrCode);
        if (noQrCodeButton != null) {
            noQrCodeButton.setOnClickListener(new ClickListener());
        }

        ListView listView = (ListView) view.findViewById(R.id.listRegatta);
        if (listView != null) {
            listView.addHeaderView(inflater.inflate(R.layout.regatta_listview_header, null));

            adapter = new RegattaAdapter(getActivity(), R.layout.ragatta_listview_row, null, 0);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new ItemClickListener());
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

    private void showNoQRCodeMessage() {
        ((StartActivity) getActivity()).showErrorPopup(R.string.no_qr_code_popup_title,
                R.string.no_qr_code_popup_message);
    }

    private boolean requestQRCodeScan() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        if (infos.size() != 0) {
            startActivityForResult(intent, requestCodeQRCode);
            return true;
        } else {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            infos = manager.queryIntentActivities(marketIntent, 0);
            if (infos.size() != 0) {
                startActivity(marketIntent);
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_play_store_and_scanning_not_available),
                        Toast.LENGTH_LONG).show();
            }
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String scanResult = data.getStringExtra("SCAN_RESULT");
            prefs.setLastScannedQRCode(scanResult);
            // handleQRCode is called in onResume()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), getString(R.string.scanning_cancelled), Toast.LENGTH_LONG).show();
        } else {
            String templateString = getString(R.string.error_scanning_qrcode);
            Toast.makeText(getActivity(), templateString.replace("{result-code}", String.valueOf(resultCode)),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handleQRCode(String qrCode) {
        ExLog.i(getActivity(), TAG, "Parsing URI: " + qrCode);
        Uri uri = Uri.parse(qrCode);
        handleScannedOrUrlMatchedUri(uri);
    }

    public void handleScannedOrUrlMatchedUri(Uri uri) {
        String uriString = uri.toString();
        CheckinManager manager = new CheckinManager(uriString, (StartActivity)getActivity());
        manager.callServerAndGenerateCheckinData();
    }

    /**
     * Display a confirmation-dialog in which the user confirms his full name and sail-id.
     * 
     * @param checkinData
     */
    public void displayUserConfirmationScreen(final CheckinData checkinData) {
        String message1 = getString(R.string.confirm_data_hello_name)
                .replace("{full_name}", checkinData.competitorName);
        String message2 = getString(R.string.confirm_data_you_are_signed_in_as_sail_id).replace("{sail_id}",
                checkinData.competitorSailId);

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
    }

    private void clearScannedQRCodeInPrefs() {
        prefs.setLastScannedQRCode(null);
    }

    /**
     * Perform a checkin request and launch RegattaAcitivity afterwards
     * 
     * TODO: Google Cloud Messaging token?
     * 
     * @param deviceMappingData
     */
    private void checkInWithAPIAndDisplayTrackingActivity(CheckinData checkinData) {
        if (DatabaseHelper.getInstance().eventLeaderboardCompetitorCombnationAvailable(getActivity(),
                checkinData.checkinDigest)) {
            try {
                DatabaseHelper.getInstance().storeCheckinRow(getActivity(), checkinData.getEvent(),
                        checkinData.getCompetitor(), checkinData.getLeaderboard(), checkinData.getCheckinUrl());

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
            Toast.makeText(getActivity(), getString(R.string.info_already_checked_in_this_qr_code), Toast.LENGTH_LONG)
                    .show();
        }
        performAPICheckin(checkinData);
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
            JSONObject requestObject = CheckinHelper.getCheckinJson(checkinData.competitorId, checkinData.deviceUid,
                    "TODO push device ID!!", date.getTime());
            HttpJsonPostRequest request = new HttpJsonPostRequest(new URL(checkinData.checkinURL),
                    requestObject.toString(), getActivity());
            NetworkHelper.getInstance(getActivity())
                    .executeHttpJsonRequestAsnchronously(request, new CheckinListener(checkinData.checkinDigest),
                            new CheckinErrorListener(checkinData.checkinDigest));
        } catch (JSONException e) {
            ExLog.e(getActivity(), TAG, "Failed to generate checkin JSON: " + e.getMessage());
            displayAPIErrorRecommendRetry();
        } catch (MalformedURLException e) {
            ExLog.e(getActivity(), TAG, "Failed to perform checkin, MalformedURLException: " + e.getMessage());
            displayAPIErrorRecommendRetry();
        }
    }

    /**
     * Shows a pop-up-dialog that informs the user than an API-call has failed and recommends a retry.
     */
    private void displayAPIErrorRecommendRetry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.notify_user_api_call_failed));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Start regatta activity.
     * 
     * @param checkinDigest
     */
    private void startRegatta(String checkinDigest) {
        Intent intent = new Intent(getActivity(), RegattaActivity.class);
        intent.putExtra(getString(R.string.checkin_digest), checkinDigest);
        getActivity().startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
        case REGATTA_LOADER:
            String[] projection = new String[] { "events.event_checkin_digest", "events.event_id", "events._id",
                    "events.event_name", "events.event_server", "competitors.competitor_display_name",
                    "competitors.competitor_id", "leaderboards.leaderboard_name",
                    "competitors.competitor_country_code", "competitors.competitor_sail_id" };
            return new CursorLoader(getActivity(), AnalyticsContract.EventLeaderboardCompetitorJoined.CONTENT_URI,
                    projection, null, null, null);

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

    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.scanQr:
                requestQRCodeScan();
                break;
            case R.id.noQrCode:
                showNoQRCodeMessage();
                break;
            }
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

            String checkinDigest = cursor.getString(cursor.getColumnIndex("event_checkin_digest"));
            startRegatta(checkinDigest);
        }
    }

    private class CheckinListener implements NetworkHelperSuccessListener {

        public String checkinDigest;

        public CheckinListener(String checkinDigest) {
            this.checkinDigest = checkinDigest;
        }

        @Override
        public void performAction(JSONObject response) {
            StartActivity startActivity = (StartActivity) getActivity();
            startActivity.dismissProgressDialog();
            startRegatta(checkinDigest);
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
            startActivity.showErrorPopup(R.string.error,
                    R.string.error_could_not_complete_operation_on_server_try_again);

            DatabaseHelper.getInstance().deleteRegattaFromDatabase(getActivity(), checkinDigest);
            Toast.makeText(getActivity(), getString(R.string.error_while_receiving_server_data), Toast.LENGTH_LONG)
                    .show();
        }
    }
    
}
