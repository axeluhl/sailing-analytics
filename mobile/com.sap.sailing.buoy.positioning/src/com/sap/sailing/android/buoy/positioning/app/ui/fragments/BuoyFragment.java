package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.activities.PositioningActivity;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.buoy.positioning.app.util.PingHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.ui.customviews.OpenSansButton;
import com.sap.sailing.android.shared.ui.customviews.OpenSansTextView;
import com.sap.sailing.android.ui.fragments.BaseFragment;

public class BuoyFragment extends BaseFragment implements LocationListener {
	private OpenSansTextView markHeaderTextView;
	private OpenSansTextView lattitudeTextView;
	private OpenSansTextView longitudeTextView;
	private OpenSansTextView accuracyTextView;
	private OpenSansButton setPositionButton;
	private OpenSansButton resetPostionButton;
	private MapFragment mapFragment;
	private Location lastKnownLocation;
	private LocationManager locationManager;
	private String locationProvider;
	private pingListener pingListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_buoy_postion_detail,
				container, false);

		mapFragment = (MapFragment) getActivity().getFragmentManager()
				.findFragmentById(R.id.map);

		markHeaderTextView = (OpenSansTextView) view
				.findViewById(R.id.mark_header);
		lattitudeTextView = (OpenSansTextView) view
				.findViewById(R.id.marker_gps_latitude);
		longitudeTextView = (OpenSansTextView) view
				.findViewById(R.id.marker_gps_longitude);
		accuracyTextView = (OpenSansTextView) view
				.findViewById(R.id.marker_gps_accuracy);
		Clicklistener clicklistener = new Clicklistener();

		setPositionButton = (OpenSansButton) view
				.findViewById(R.id.marker_set_position_button);
		setPositionButton.setOnClickListener(clicklistener);

		resetPostionButton = (OpenSansButton) view
				.findViewById(R.id.marker_reset_position_button);
		resetPostionButton.setOnClickListener(clicklistener);
		resetPostionButton.setVisibility(View.GONE);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		initLocationProvider();
		MarkInfo mark = ((PositioningActivity) getActivity()).getMarkInfo();
		if (mark != null) {
			markHeaderTextView.setText(mark.getName());
			setUpPingUI();
		}
	}

	public void setUpPingUI() {
		MarkPingInfo markPing = ((PositioningActivity) getActivity())
				.getMarkPing();
		if (markPing != null) {
			lattitudeTextView.setText(markPing.getLattitude());
			longitudeTextView.setText(markPing.getLongitude());
			accuracyTextView.setText("~" + markPing.getAccuracy());
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		lastKnownLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		initLocationProvider();
	}

	@Override
	public void onProviderEnabled(String provider) {
		initLocationProvider();
	}

	@Override
	public void onProviderDisabled(String provider) {
		initLocationProvider();
	}

	private void initLocationProvider() {
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
		locationProvider = locationManager
				.getBestProvider(new Criteria(), true);
		locationManager.requestLocationUpdates(locationProvider, 60000, 10,
				this);
	}

	public void setPingListener(pingListener listener) {
		pingListener = listener;
	}

	public interface pingListener {
		public void updatePing();
	}

	private class Clicklistener implements OnClickListener {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.marker_set_position_button) {
				PingHelper helper = new PingHelper();
				try {
					if (lastKnownLocation != null) {
						MarkInfo mark = ((PositioningActivity) getActivity())
								.getMarkInfo();
						LeaderboardInfo leaderboard = ((PositioningActivity) getActivity())
								.getLeaderBoard();
						helper.storePingInDatabase(getActivity(),
								lastKnownLocation, mark);
						helper.sendPingToServer(getActivity(),
								lastKnownLocation, leaderboard, mark);
						pingListener.updatePing();
						setUpPingUI();
					}
					else{
						Toast.makeText(getActivity(), "Location is not available yet", Toast.LENGTH_LONG).show();
					}
				} catch (GeneralDatabaseHelperException e) {
					e.printStackTrace();
				}
			} else if (id == R.id.marker_reset_position_button) {
				// Reset position
			}

		}

	}

}
