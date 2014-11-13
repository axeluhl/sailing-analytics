package com.sap.sailing.android.tracking.app.ui.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.adapter.RegattaAdapter;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.BackendHelper;
import com.sap.sailing.android.tracking.app.utils.CheckinQRCodeHelper;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;

public class HomeFragment extends BaseFragment implements LoaderCallbacks<Cursor> {

    private final static String TAG = HomeFragment.class.getName();
    private final static String REQUEST_TAG = "request_homefragment";
    private final static int REGATTA_LOADER = 1;

    private AppPreferences prefs;
    
    private final JsonSerializer<RaceLogEvent> eventSerializer = RaceLogEventSerializer
            .create(new CompetitorJsonSerializer());
    private Button scan;
    private int requestCodeQRCode = 442;
    private RegattaAdapter mAdapter;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        prefs = new AppPreferences(getActivity());
        
        getActivity().getContentResolver();

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        scan = (Button) view.findViewById(R.id.scanQr);
        if (scan != null) {
            scan.setOnClickListener(new ClickListener());
        }

        ListView listView = (ListView) view.findViewById(R.id.listRegatta);
        if (listView != null) {
            listView.addHeaderView(inflater.inflate(R.layout.regatta_listview_header, null));
            mAdapter = new RegattaAdapter(getActivity(), R.layout.ragatta_listview_row, null, 0);
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(new ItemClickListener());
        }

        getLoaderManager().initLoader(REGATTA_LOADER, null, this);

        return view;
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
                Toast.makeText(getActivity(), "PlayStore and Scanning not available.", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String scanResult = data.getStringExtra("SCAN_RESULT");
            
            ExLog.i(getActivity(), TAG, "Parsing URI: " + scanResult);
            
            Uri uri = Uri.parse(scanResult);
            
            final String server = uri.getScheme() + "://" + uri.getHost();
            final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
            
            prefs.setServerURL(server + ":" + port);
            
            String leaderboardName;
			try {
				leaderboardName = URLEncoder.encode(uri.getQueryParameter(CheckinQRCodeHelper.LEADERBOARD_NAME), "UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				ExLog.e(getActivity(), TAG, "Failed to encode leaderboard name: " + e.getMessage());
				leaderboardName = "";
			}
			
            final String competitorId = uri.getQueryParameter(CheckinQRCodeHelper.COMPETITOR_ID);
            final String checkinURLStr = prefs.getServerURL() + prefs.getServerCheckinPath().replace("{leaderboard-name}", leaderboardName);

            DeviceIdentifier deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(prefs.getDeviceIdentifier()));
            Date date= new java.util.Date();
            
            // TODO: Push notification token
            
            try {
				JSONObject requestObject = CheckinQRCodeHelper.getCheckinJson(competitorId, deviceUuid.getStringRepresentation(), "TODO!!", date.getTime());
				JsonObjectRequest request = new JsonObjectRequest(checkinURLStr, requestObject, new CheckinListener(), new CheckinErrorListener());
				VolleyHelper.getInstance(getActivity()).addRequest(request);
				
			} catch (JSONException e) {
				ExLog.e(getActivity(), TAG, "Failed to generate checkin JSON: " + e.getMessage());
			}
            
            
            
            
//            
//            Competitor competitor = new CompetitorImpl(competitorId, null, null, null, null);
//            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(
//                    MillisecondsTimePoint.now(), AppPreferences.raceLogEventAuthor, device, competitor, 0, from, to);
//            String eventJson = eventSerializer.serialize(event).toString();
//            
//            
//            
//            DeviceMappingRequest dataRequest = new DeviceMappingRequest(deviceMapping, eventJson,
//                    new DeviceMappingListener(), new DeviceMappingErrorListener());
//            
            
//            VolleyHelper.getInstance(getActivity()).addRequest(dataRequest, REQUEST_TAG);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), "Scanning canceled", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Error scanning QRCode (" + resultCode + ")", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        VolleyHelper.getInstance(getActivity()).cancelRequest(REQUEST_TAG);
    }

    private void startRegatta(String eventId, String competitorId) {
        getActivity().startActivity(new Intent(getActivity(), RegattaActivity.class));
    }

    private JsonObjectRequest checkInRequest(String server, final String eventId, final String competitorId) {
        JSONObject json = new JSONObject();
        try {
            json.put("deviceUdid", prefs.getDeviceIdentifier());
            json.put("deviceType", "android");
            json.put("pushDeviceId", "notImplementedYet");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        JsonObjectRequest result = new JsonObjectRequest(BackendHelper.getUrl(server, eventId, competitorId, "device"),
                json, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject result) {
                        startRegatta(eventId, competitorId);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = null;
                        NetworkResponse response = error.networkResponse;
                        if (response != null) {
                            if (response.statusCode == HttpStatus.SC_NOT_FOUND) {
                                message = getString(R.string.homefragment_volley_404);
                            } else {
                                message = String.format(getString(R.string.homefragment_volley_unknown),
                                        response.statusCode);
                            }
                        } else {
                            message = getString(R.string.homefragment_volley_unexpected);
                            ExLog.i(getActivity(), TAG, error.toString());
                        }
                        if (message != null) {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
        case REGATTA_LOADER:
            String[] projection = new String[] { Event._ID, Event.EVENT_TITLE, Event.EVENT_SERVER,
                    com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor.COMPETITOR_NAME,
                    Event.EVENT_ID, com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor.COMPETITOR_ID };
            return new CursorLoader(getActivity(), AnalyticsContract.EventCompetitor.CONTENT_URI, projection, null,
                    null, null);

        default:
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
        case REGATTA_LOADER:
            mAdapter.changeCursor(cursor);
            break;

        default:
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
        case REGATTA_LOADER:
            mAdapter.changeCursor(null);
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
            }
        }
    }

    private class ItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            if (cursor.moveToFirst()) {
                prefs.setServerURL(cursor.getString(cursor.getColumnIndex(Event.EVENT_SERVER)));
                String eventId = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID));
                String competitorId = cursor
                        .getString(cursor
                                .getColumnIndex(com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor.COMPETITOR_ID));

                VolleyHelper.getInstance(getActivity()).addRequest(
                        checkInRequest(prefs.getServerURL(), eventId, competitorId));
            }
        }
    }

    private class CheckinListener implements Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            ExLog.i(getActivity(), TAG, response.toString());
            Intent intent = new Intent(getActivity(), RegattaActivity.class);
            startActivity(intent);
        }
    }

    private class CheckinErrorListener implements ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            ExLog.e(getActivity(), TAG, error.getMessage().toString());
            Toast.makeText(getActivity(), "Error while receiving server data", Toast.LENGTH_LONG).show();
        }
    }

}
