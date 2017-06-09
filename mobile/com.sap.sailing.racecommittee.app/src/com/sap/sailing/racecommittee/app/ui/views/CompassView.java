package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;

public class CompassView extends RelativeLayout {

    private CompassDirectionListener changeListener;
    private RotateAnimation rotation;
    private ImageView needleView;
    private BackAwareEditText degreeView;
    private TextView degreeValue;
    private float currentDegrees;
    private Float deferredToDegrees;

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

    private float getNeedlePivotX() {
        return needleView.getMeasuredWidth() / 2;
    }

    private float getNeedlePivotY() {
        return needleView.getMeasuredHeight() / 2;
    }

    private void inflate() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.compass_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        needleView = (ImageView) findViewById(R.id.compass_view_needle);
        degreeValue = (TextView) findViewById(R.id.compass_view_value);
        degreeView = (BackAwareEditText) findViewById(R.id.compass_view_degree);
        degreeView.setSelectAllOnFocus(true);
        degreeView.setInputDownPressedListener(new BackAwareEditText.InputDownPressedListener() {
            @Override
            public void onImeBack(BackAwareEditText editText) {
                float degree = currentDegrees > 0 ? currentDegrees : currentDegrees + 360;
                degreeView.clearFocus();
                degreeView.setText(String.format("%.0f°", degree));
                degreeValue.setText(degreeView.getText());
            }
        });
        degreeView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String value = degreeView.getText().toString();
                    value = value.replace("°", "");
                    value = value.replace(" ", "");
                    if (TextUtils.isEmpty(value)) {
                        value = "0";
                    }
                    float degree = Float.valueOf(value);
                    if (degree >= 0 && degree <= 360) {
                        setDirection(degree);
                    } else {
                        generateAndShowAlert();
                    }
                    return true;
                }
                return false;
            }
        });

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void generateAndShowAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_AlertDialog);
        builder.setTitle(getContext().getString(R.string.error_wrong_degree_value_title));
        builder.setMessage(getContext().getString(R.string.error_wrong_degree_value_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                degreeView.requestFocus();
                degreeView.selectAll();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(degreeView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        dialog.show();
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

    public float getDirection() {
        return currentDegrees;
    }

    public void setDirection(float toDegrees) {
        if (isNeedleReady()) {
            deferredToDegrees = toDegrees;
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
        if (degreeView != null) {
            float degree = toDegrees;
            if (degree < 0) {
                degree += 360;
            }
            if (degree >= 359.5f) {
                degree = 0;
            }
            degreeView.setText(String.format("%.0f°", degree));
            degreeValue.setText(degreeView.getText());
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
            requestFocus();
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

    public void setReadOnly(boolean readOnly) {
        degreeView.setVisibility(GONE);
        degreeValue.setVisibility(GONE);

        if (readOnly) {
            degreeValue.setVisibility(VISIBLE);
        } else {
            degreeView.setVisibility(VISIBLE);
        }
    }

    private boolean isNeedleReady() {
        return getNeedlePivotX() == 0.0;
    }

    public interface CompassDirectionListener {
        void onDirectionChanged(float degree);
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
