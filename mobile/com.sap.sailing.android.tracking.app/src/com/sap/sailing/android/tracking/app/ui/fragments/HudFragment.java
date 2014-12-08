package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;

public class HudFragment extends BaseFragment {
	
	private static final String TAG = HudFragment.class.getName();
	
	private float maxTranslateY;
	private float minTranslateY;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_hud, container, false);
		
		FrameLayout layout = (FrameLayout)getActivity().findViewById(R.id.hud_content_frame);
		layout.setOnTouchListener(new OverlayOnTouchListener());
		
		return view;
	}
	
	public void setHeading(float heading)
	{
		TextView headingLabel = (TextView)getActivity().findViewById(R.id.hud_hdg_label);
		headingLabel.setText(getString(R.string.hud_heading_prefix) + String.valueOf(Math.round(heading)) + "°");
	}
	
	public void setSpeedOverGround(float speedInMetersPerSeconds)
	{
		float speedInKnots = speedInMetersPerSeconds * 1.9438444924574f;
		TextView speedLabel = (TextView)getActivity().findViewById(R.id.hud_speed_label);
		speedLabel.setText(getString(R.string.hud_speed_over_ground_prefix) + String.valueOf(speedInKnots) + "kn");
	}
	
	/**
	 * called from activity
	 */
	public void layoutOverlay() {
		FrameLayout layout = (FrameLayout)getActivity().findViewById(R.id.hud_content_frame);
		maxTranslateY = layout.getHeight()- dpToPx(40);
		
		View modeLabel = (View)getActivity().findViewById(R.id.mode_label);
		float heightOfOneDataRow = modeLabel.getHeight();
		minTranslateY = layout.getTranslationY() + (heightOfOneDataRow * 2);
		
		layout.setTranslationY(maxTranslateY);
	}
	
	class OverlayOnTouchListener implements OnTouchListener {
        private int yDelta;
        private int lastTranslation;
        boolean isUp = false;
        
		@Override
		public boolean onTouch(View view, MotionEvent event) {
            if (view.getId() != R.id.hud_content_frame) return false;
            
            final int y = (int) event.getRawY();
            
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: 
				 int newTranslation = y - yDelta;
				 lastTranslation = newTranslation;
				 if (newTranslation >= minTranslateY && newTranslation < maxTranslateY)
				 {
					 view.setTranslationY(newTranslation);
				 }
				break;
			case MotionEvent.ACTION_UP:
				int treshold = Math.round((maxTranslateY - minTranslateY) / 2);
				if (lastTranslation < treshold) {
					animateUp(view);
				} else {
					animateDown(view);
				}
				
				long eventDuration = 
			            android.os.SystemClock.elapsedRealtime() 
			            - event.getDownTime();
				
				if (eventDuration < 200) {
					view.performClick();
					if (!isUp) 
					{
						animateUp(view);
					}
				}
				
				break;

			case MotionEvent.ACTION_DOWN:
	            yDelta = y - (int)Math.round(view.getTranslationY());
				break;
				
			default:
				break;
			}

			return true;
		}
		
		private void animateUp(View view) {
			view.animate().translationY(minTranslateY).setDuration(100)
					.setInterpolator(new LinearInterpolator()).start();

			lastTranslation = (int) minTranslateY;
			isUp = true;
		}

		private void animateDown(View view) {
			view.animate().translationY(maxTranslateY).setDuration(500)
					.setInterpolator(new BounceInterpolator()).start();

			lastTranslation = (int) maxTranslateY;
			isUp = false;
		}
	}
}
