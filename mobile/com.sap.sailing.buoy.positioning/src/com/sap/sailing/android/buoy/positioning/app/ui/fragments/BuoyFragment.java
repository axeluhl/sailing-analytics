package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.activities.PositioningActivity;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.ui.customviews.OpenSansButton;
import com.sap.sailing.android.shared.ui.customviews.OpenSansTextView;
import com.sap.sailing.android.ui.fragments.BaseFragment;


public class BuoyFragment extends BaseFragment{
	private OpenSansTextView markHeaderTextView;
	private OpenSansTextView lattitudeTextView;
	private OpenSansTextView longitudeTextView;
	private OpenSansTextView accuracyTextView;
	private OpenSansButton setPositionButton;
	private OpenSansButton resetPostionButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_buoy_postion_detail, container, false);
		markHeaderTextView = (OpenSansTextView) view.findViewById(R.id.mark_header);
		lattitudeTextView = (OpenSansTextView) view.findViewById(R.id.marker_gps_latitude);
		longitudeTextView = (OpenSansTextView) view.findViewById(R.id.marker_gps_longitude);
		accuracyTextView = (OpenSansTextView) view.findViewById(R.id.marker_gps_accuracy);
		Clicklistener clicklistener = new Clicklistener();
		
		setPositionButton = (OpenSansButton) view.findViewById(R.id.marker_set_position_button);
		setPositionButton.setOnClickListener(clicklistener);
		
		resetPostionButton = (OpenSansButton) view.findViewById(R.id.marker_reset_position_button);
		resetPostionButton.setOnClickListener(clicklistener);
		return view;
	}
	
	@Override
    public void onResume() {
        super.onResume();
        MarkInfo mark = ((PositioningActivity)getActivity()).getMarkInfo();
        MarkPingInfo markPing = ((PositioningActivity)getActivity()).getMarkPing();
        if(mark != null)
        {
        	markHeaderTextView.setText(mark.getName());
        	if(markPing != null)
        	{
        		lattitudeTextView.setText(markPing.getLattitude());
        		longitudeTextView.setText(markPing.getLongitude());
        		accuracyTextView.setText("~" + markPing.getAccuracy());
        	}
        }
	}
	
	private class Clicklistener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.marker_set_position_button)
			{
				// Set Position
			}
			else if(id == R.id.marker_reset_position_button)
			{
				// Reset position
			}
			
		}
		
	}

}
