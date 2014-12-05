package com.sap.sailing.android.tracking.app.ui.fragments;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.sap.sailing.android.tracking.app.R;

public class HudFragment extends BaseFragment {
	
	private static final String TAG = HudFragment.class.getName();
	
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
//	public void layoutOverlay() {
//		FrameLayout layout = (FrameLayout)getActivity().findViewById(R.id.hud_content_frame);
//		
//		Configuration configuration = getActivity().getResources().getConfiguration();
//		int screenHeight = configuration.screenHeightDp;
//		
//		float bottomNotificationBarHeightPx = TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_DIP, 40, getActivity().getResources().getDisplayMetrics());
//		
//	
//		
//		float actionbarHeightPixels = TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_DIP, getActionBarHeight(), getActivity().getResources().getDisplayMetrics());
//		
//		float translateYPixels = screenHeight  / 2; //- bottomNotificationBarHeightPx - actionbarHeightPixels;
//		// TODO FIX HUGE MESS.
//		
//		//FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)layout.getLayoutParams();
//		//params.topMargin = Math.round(dpToPx(screenHeight - 100));
//		System.out.println("40 in PX = " + dpToPx(40));
//		System.out.println("100 in DP = " + pxToDp(100));
//		System.out.println("SCREEN HEIGHT IN DP = " + screenHeight);
//		System.out.println("SCREEN HEIGHT IN PX = " + dpToPx(screenHeight));
//		System.out.println("ACTION BAR IN DP = " + getActionBarHeight());
//		System.out.println("ACTION BAR IN PX = " + dpToPx(getActionBarHeight()));
//		
//		RelativeLayout r = (RelativeLayout) ((ViewGroup) layout.getParent()).getParent();
//	
//		System.out.println("PARENT HEIGHT = " + r.getHeight());
//		
//		layout.setTranslationY(Math.round(dpToPx(screenHeight - 40 - pxToDp(getActionBarHeight()) - getStatusBarHeight() )));
//		//layout.setTranslationY(dpToPx(screenHeight / 2));
//		
//	}
	
	private float dpToPx(float dp)
	{
		float density = getActivity().getResources().getDisplayMetrics().density;
		return dp * density;
	}

	private float pxToDp(float px)
	{
		float density = getActivity().getResources().getDisplayMetrics().density;
		return px / density;
	}
	
	private  int getStatusBarHeight() { 
	      int result = 0;
	      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = getResources().getDimensionPixelSize(resourceId);
	      } 
	      return result;
	} 
	
	private int getActionBarHeight() {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
					true))
				actionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, getResources().getDisplayMetrics());
		} else {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
					getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}
	
	class OverlayOnTouchListener implements OnTouchListener {
		
		private float actionBarHeight = 0;
		private int xDelta;
        private int yDelta;
		
		public OverlayOnTouchListener() {
			TypedValue tv = new TypedValue();
			if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
			{
			    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
			}
		}

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			//FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (view.getId() != R.id.hud_content_frame) return false;
            
            final int y = (int) event.getRawY();
            
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: 
		         view.setTranslationY(y - yDelta);
		         System.out.println("translationY = " + view.getTranslationY());
				break;

			case MotionEvent.ACTION_UP:
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
