package com.sap.sailing.racecommittee.app.ui.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.*;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.DialogBuilder;
import com.sap.sailing.racecommittee.app.utils.ColorHelper;

import java.util.zip.Inflater;

public class CompassView extends RelativeLayout {

    public interface CompassDirectionListener {
        void onDirectionChanged(float degree);
    }

    private CompassDirectionListener changeListener = null;
    private RotateAnimation rotation = null;
    private ImageView needleView = null;
    private TextView degreeView = null;
    private float currentDegrees = 0.0f;
    private Float deferredToDegrees = null;

    private float getNeedlePivotX() { return needleView.getMeasuredWidth() / 2; }
    private float getNeedlePivotY() { return needleView.getMeasuredHeight() / 2; }

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

        View numbers = findViewById(R.id.numbers);
        if (numbers != null) {
            numbers.setOnLongClickListener(new ManualEntry());
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        needleView = (ImageView) findViewById(R.id.compass_view_needle);
        degreeView = (TextView) findViewById(R.id.compass_view_degree);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rotateX = (-1) * (event.getY() - needleView.getY() - getNeedlePivotY());
        float rotateY = event.getX() - needleView.getX() - getNeedlePivotX();

        float toDegrees = (float) Math.toDegrees(Math.atan2(rotateY, rotateX));
        cancelAndStartAnimation(toDegrees, 0);
        
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        doDeferredRotation();
    }

    public void setDirection(float toDegrees) {
        if (isNeedleReady()) {
            deferredToDegrees = toDegrees;
        } else {
            cancelAndStartAnimation(toDegrees, 500);
        }
    }

    public float getDirection() {
        return currentDegrees;
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
        if (degreeView != null) {
            float degree = toDegrees;
            if (degree < 0) {
                degree += 360;
            }
            degreeView.setText(String.format("%.0f", degree));
        }
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
        if (deferredToDegrees != null) {
            float toDegrees = deferredToDegrees;
            deferredToDegrees = null;
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

    private class ManualEntry implements OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(currentDegrees));

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.degrees);
            builder.setView(input);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setDirection(Float.parseFloat(input.getText().toString()));
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setCancelable(true);
            builder.create().show();
            return false;
        }
    }
}
