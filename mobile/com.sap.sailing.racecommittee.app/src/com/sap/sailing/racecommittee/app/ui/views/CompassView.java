package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sap.sailing.racecommittee.app.R;

public class CompassView extends RelativeLayout {
	
	public interface CompassDirectionListener {
		public void onDirectionChanged(float degree);
	}

	private CompassDirectionListener changeListener = null;
	private RotateAnimation rotation = null;
	private ImageView needleView;
	private float fromDegrees = 0.0f;
	private float rotateDegrees = 0.0f;
	
	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflate(context);
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context);
	}

	public CompassView(Context context) {
		super(context);
		inflate(context);
	}	
	
	public void setDirectionListener(CompassDirectionListener listener) {
		changeListener = listener;
	}

	private void inflate(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.compass_view, this);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		needleView = (ImageView) findViewById(R.id.compass_view_needle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		float needlePivotX = needleView.getWidth() / 2;
		float needlePivotY = needleView.getHeight() / 2;
		
		float rotateX = event.getX() - needleView.getX() - needlePivotX;
		float rotateY = (-1) * (event.getY() - needleView.getY() - needlePivotY);
		
		rotateDegrees = (float)Math.toDegrees(Math.atan2(rotateX, rotateY));
		
		cancelAnimation(needlePivotX, needlePivotY, rotateDegrees);
		rotation.setDuration(0);
		rotation.setFillAfter(true);
		rotation.setAnimationListener(new NeedleRotationListener());
		needleView.startAnimation(rotation);
		
		return true;
	}
	
	private void cancelAnimation(float pivotX, float pivotY, float toDegrees) {
		if (rotation != null) {
			if (!rotation.hasEnded()) {
				rotation.cancel();
			}
		}
		rotation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
	}
	
	private void notifyListener() {
		if (changeListener != null) {
			float degree = fromDegrees > 0 ? fromDegrees : fromDegrees + 360;
			if (degree == 360 || degree == -360) {
				degree = 0;
			}
			changeListener.onDirectionChanged(degree);
		}
	}
	
	private class NeedleRotationListener implements AnimationListener {

		public void onAnimationEnd(Animation animation) {
			if (animation.hasEnded()) {
				fromDegrees = rotateDegrees;
				if (CompassView.this.changeListener != null) {
					notifyListener();
				}
			}
		}

		public void onAnimationRepeat(Animation animation) { }

		public void onAnimationStart(Animation animation) { }
		
	}

}
