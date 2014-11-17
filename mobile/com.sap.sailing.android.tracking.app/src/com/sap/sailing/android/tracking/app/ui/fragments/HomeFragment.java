package com.sap.sailing.android.tracking.app.ui.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.preference.DialogPreference;
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
            
            String leaderboardNameFromQR;
			try {
				leaderboardNameFromQR = URLEncoder.encode(uri.getQueryParameter(CheckinQRCodeHelper.LEADERBOARD_NAME), "UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				ExLog.e(getActivity(), TAG, "Failed to encode leaderboard name: " + e.getMessage());
				leaderboardNameFromQR = "";
			}
			
            final String competitorId = uri.getQueryParameter(CheckinQRCodeHelper.COMPETITOR_ID);
            final String checkinURLStr = prefs.getServerURL() + prefs.getServerCheckinPath().replace("{leaderboard-name}", leaderboardNameFromQR);
            final String eventId = uri.getQueryParameter(CheckinQRCodeHelper.EVENT_ID);
            final String leaderboardName = leaderboardNameFromQR;
            
            final DeviceIdentifier deviceUuid = new SmartphoneUUIDIdentifierImpl(UUID.fromString(prefs.getDeviceIdentifier()));
            final Date date= new java.util.Date();
            
            
            // TODO: GET EVENT, LEADERBOARD, COMPETITOR
            
            
            
            // There are 5 Stages after the QR-Code scan is complete:
            //   1. Get Event
            //   2. Get Leaderboard
            //   3. Get Competitor
            //   4. Let user confirm that the information is correct
            //   5. Checkin
            
            
            final String getEventUrl = prefs.getServerURL() + prefs.getServerEventPath(eventId);
            final String getLeaderboardUrl = prefs.getServerURL() + prefs.getServerLeaderboardPath(leaderboardName);
            final String getCompetitorUrl = prefs.getServerURL() + prefs.getServerCompetitorPath(competitorId);
            
            JsonObjectRequest getEventRequest = new JsonObjectRequest(getEventUrl, null, new Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
					System.out.println("got response: " + response);
					// TODO: get data, eventually save to db
					
					JsonObjectRequest getLeaderboardRequest = new JsonObjectRequest(getLeaderboardUrl,null, new Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							System.out.println("got response: " + response);
							// TODO: get data, eventually save to db
							
							JsonObjectRequest getCompetitorRequest = new JsonObjectRequest(getCompetitorUrl, null, new Listener<JSONObject>() {
								@Override
								public void onResponse(JSONObject response) {
									System.out.println("got response: " + response);
									// TODO: get data, eventually save to db
									displayUserConfirmationScreen("TODO", "TODO"); 
								}
							}, new ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError error) {
									ExLog.e(getActivity(), TAG, "Failed to get competitor from API: " + error.getMessage());
									displayAPIErrorRecommendRetry();	
								}
							});
							
							VolleyHelper.getInstance(getActivity()).addRequest(getCompetitorRequest);
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							ExLog.e(getActivity(), TAG, "Failed to get leaderboard from API: " + error.getMessage());
							displayAPIErrorRecommendRetry();							
						}
					});
					
					VolleyHelper.getInstance(getActivity()).addRequest(getLeaderboardRequest);
				}
			}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							ExLog.e(getActivity(), TAG, "Failed to get event from API: " + error.getMessage());
							displayAPIErrorRecommendRetry();
						}
			});
            
            
            
            VolleyHelper.getInstance(getActivity()).addRequest(getEventRequest);
            
            
            
            // TODO: Push notification token
            

//            
//            
            
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
//            VolleyHelper.getInstance(getActivity()).addRequest(dataRequest, REQUEST_TAG);
// 
            
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
    
    private void displayUserConfirmationScreen(String fullNameOfUser, String sailId, final DeviceMappingData deviceMappingData)
    {
    	String message1 = getString(R.string.confirm_data_hello_name).replace("{full_name}", fullNameOfUser);
    	String message2 = getString(R.string.confirm_data_you_are_signed_in_as_sail_id).replace("{sail_id}", sailId);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message1 + "\n\n" + message2);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.confirm_data_is_correct), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				checkInWithAPIAndDisplayTrackingActivity(deviceMappingData);
			}
			
        }).setNegativeButton(R.string.decline_data_is_incorrect, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
	
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    /**
	 * Perform a checking request and launch RegattaAcitivity afterwards
	 * 
	 * TODO: Google Cloud Messaging token?
	 * 
	 * @param deviceMappingData
	 */
	private void checkInWithAPIAndDisplayTrackingActivity(
			DeviceMappingData deviceMappingData) {
		Date date = new Date();

		try {
			JSONObject requestObject = CheckinQRCodeHelper.getCheckinJson(
					deviceMappingData.competitorId,
					deviceMappingData.deviceUid, "TODO!!", date.getTime());

			JsonObjectRequest checkinRequest = new JsonObjectRequest(
					deviceMappingData.hostUrl, requestObject,
					new CheckinListener(deviceMappingData.leaderboardName),
					new CheckinErrorListener());

			VolleyHelper.getInstance(getActivity()).addRequest(checkinRequest);

		} catch (JSONException e) {
			ExLog.e(getActivity(), TAG,
					"Failed to generate checkin JSON: " + e.getMessage());
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
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {

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
	 * @param regattaName
	 * @param eventName
	 */
    private void startRegatta(String regattaName, String eventName) {
    	Intent intent = new Intent(getActivity(), RegattaActivity.class);
    	intent.putExtra(getString(R.string.regatta_name), regattaName);
    	intent.putExtra(getString(R.string.event_name), eventName);
        getActivity().startActivity(intent);
    }

//    private JsonObjectRequest checkInRequest(String server, final String eventId, final String competitorId) {
//        JSONObject json = new JSONObject();
//        try {
//            json.put("deviceUdid", prefs.getDeviceIdentifier());
//            json.put("deviceType", "android");
//            json.put("pushDeviceId", "notImplementedYet");
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//        }
//        JsonObjectRequest result = new JsonObjectRequest(BackendHelper.getUrl(server, eventId, competitorId, "device"),
//                json, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject result) {
//                       // startRegatta(eventId, competitorId);
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        String message = null;
//                        NetworkResponse response = error.networkResponse;
//                        if (response != null) {
//                            if (response.statusCode == HttpStatus.SC_NOT_FOUND) {
//                                message = getString(R.string.homefragment_volley_404);
//                            } else {
//                                message = String.format(getString(R.string.homefragment_volley_unknown),
//                                        response.statusCode);
//                            }
//                        } else {
//                            message = getString(R.string.homefragment_volley_unexpected);
//                            ExLog.i(getActivity(), TAG, error.toString());
//                        }
//                        if (message != null) {
//                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//
//        return result;
//    }

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
    
    
    private class DeviceMappingData
    {
    	//public String gcmId;
    	public String leaderboardName;
    	public String hostUrl;
    	public String competitorId;
    	public String deviceUid;
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
    	
    	public String leaderboardName;
    	
    	public CheckinListener(String leaderboardName) {
    		try {
				this.leaderboardName = URLDecoder.decode(leaderboardName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				ExLog.e(getActivity(), TAG, "UnsupportedEncodingException: " + e.getMessage());
			}
		}

        @Override
        public void onResponse(JSONObject response) {
        	// TODO: twice the same string here
            startRegatta(leaderboardName, leaderboardName);
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
