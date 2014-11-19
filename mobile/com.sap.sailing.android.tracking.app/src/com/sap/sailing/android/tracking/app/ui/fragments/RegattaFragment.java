package com.sap.sailing.android.tracking.app.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;

public class RegattaFragment extends BaseFragment implements OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_regatta, container, false);
		
		Button startTracking = (Button) view.findViewById(R.id.start_tracking);
		startTracking.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.start_tracking:
			showTrackingActivity();
			break;
		default:
			break;
		}
	}
	
	private void showTrackingActivity() {
		Intent intent = new Intent(getActivity(), TrackingActivity.class);
		getActivity().startActivity(intent);
	}

	private void stopTracking() {
		Intent intent = new Intent(getActivity(), TrackingService.class);
		intent.setAction(getString(R.string.tracking_service_stop));
		getActivity().startService(intent);
	}

	private void showStopTrackingConfirmationDialog() {
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.please_confirm)
				.setMessage(R.string.do_you_really_want_to_stop_tracking)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								stopTracking();
							}
						}).setNegativeButton(android.R.string.no, null).create();
		
		dialog.show();
	}
}
