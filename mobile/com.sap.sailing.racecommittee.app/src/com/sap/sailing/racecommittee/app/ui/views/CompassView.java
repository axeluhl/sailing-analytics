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
    private ImageView needleView = null;
    private float currentDegrees = 0.0f;
    private Float deferredToDegress = null;

    private float getNeedlePivotX() { return needleView.getWidth() / 2; }
    private float getNeedlePivotY() { return needleView.getHeight() / 2; }

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate();
    }

    public CompassView(Context context) {
        super(context);
        inflate();
    }

    private void inflate() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compass_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        needleView = (ImageView) findViewById(R.id.compass_view_needle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rotateX = event.getX() - needleView.getX() - getNeedlePivotX();
        float rotateY = (-1) * (event.getY() - needleView.getY() - getNeedlePivotY());

        float toDegrees = (float) Math.toDegrees(Math.atan2(rotateX, rotateY));
        cancelAndStartAnimation(toDegrees, 0);
        
        return true;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        doDeferredRotation();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setDirection(float toDegrees) {
        if (isNeedleReady()) {
            deferredToDegress = toDegrees;
        } else {
            cancelAndStartAnimation(toDegrees, 500);
        }
    }

    private void cancelAndStartAnimation(float toDegrees, long duration) {
        if (rotation != null) {
            if (!rotation.hasEnded()) {
                rotation.cancel();
            }
        }
        rotation = new RotateAnimation(currentDegrees, toDegrees, getNeedlePivotX(), getNeedlePivotY());
        rotation.setDuration(duration);
        rotation.setFillAfter(true);
        rotation.setAnimationListener(new NeedleRotationListener(toDegrees));
        needleView.startAnimation(rotation);
    }

    public void setDirectionListener(CompassDirectionListener listener) {
        changeListener = listener;
    }

    private void notifyListener() {
        if (changeListener != null) {
            float degree = currentDegrees > 0 ? currentDegrees : currentDegrees + 360;
            if (Math.abs(degree) == 360) {
                degree = 0;
            }
            changeListener.onDirectionChanged(degree);
        }
    }
    
    private void doDeferredRotation() {
        if (deferredToDegress != null) {
            float toDegrees = deferredToDegress;
            deferredToDegress = null;
            setDirection(toDegrees);
        }
    }
    
    private boolean isNeedleReady() {
        return getNeedlePivotX() == 0.0;
    }

    private class NeedleRotationListener implements AnimationListener {
        
        private float targetDegrees;
        
        public NeedleRotationListener(float toDegrees) {
            this.targetDegrees = toDegrees;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (animation.hasEnded()) {
                currentDegrees = targetDegrees;
                notifyListener();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

}
