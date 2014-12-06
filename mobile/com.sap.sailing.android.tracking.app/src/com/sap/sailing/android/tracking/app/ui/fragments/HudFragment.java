package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

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

		@Override
		public boolean onTouch(View view, MotionEvent event) {
            if (view.getId() != R.id.hud_content_frame) return false;
            
            final int y = (int) event.getRawY();
            
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: 
				 int newTranslation = y - yDelta;
				 if (newTranslation >= minTranslateY && newTranslation < maxTranslateY)
				 {
					 view.setTranslationY(newTranslation);
					 lastTranslation = newTranslation;
				 }
				break;
			case MotionEvent.ACTION_UP:
				int treshold = Math.round((maxTranslateY - minTranslateY) / 2);
				if (lastTranslation < treshold) {
					view.animate().translationY(minTranslateY).setDuration(500)
							.setInterpolator(new LinearInterpolator()).start();

					lastTranslation = (int) minTranslateY;
				} else {
					view.animate().translationY(maxTranslateY).setDuration(100)
							.setInterpolator(new BounceInterpolator()).start();

					lastTranslation = (int) maxTranslateY;
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
	}
}
