package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class PanelButton extends FrameLayout implements View.OnClickListener, DialogInterface.OnClickListener {

    public final static int LEVEL_UNKNOWN = -1;
    public final static int LEVEL_NORMAL = 0;
    public final static int LEVEL_TOGGLED = 1;

    private View mLayer;
    private View mLock;
    private TextView mHeader;

    private View mContent;
    private TextView mText;
    private ImageView mImage;
    private Switch mSwitch;

    private ImageView mMarker;
    private View mLine;

    private int mType;

    private PanelButtonClick mListener;

    public PanelButton(Context context) {
        this(context, null);
    }

    public PanelButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PanelButton);

        setPanelType(a.getInt(R.styleable.PanelButton_buttonType, 0));

        switch (mType) {
            default:
                inflate(getContext(), R.layout.panel_button_setup, this);
        }

        mLayer = findViewById(R.id.panel_layer);
        mContent = findViewById(R.id.panel_content);

        mHeader = (TextView) findViewById(R.id.panel_button_header);
        setHeader(a.getString(R.styleable.PanelButton_buttonHeader));

        mText = (TextView) findViewById(R.id.panel_value);
        setPanelText(a.getString(R.styleable.PanelButton_buttonValue));

        mImage = (ImageView) findViewById(R.id.panel_image);
        setPanelImage(a.getDrawable(R.styleable.PanelButton_buttonImage));

        mSwitch = (Switch) findViewById(R.id.panel_switch);
        setPanelSwitch(a.getString(R.styleable.PanelButton_buttonSwitch));
        if (mSwitch != null) {
            mSwitch.setOnClickListener(this);
        }

        mLock = findViewById(R.id.panel_lock);
        setLock(a.getBoolean(R.styleable.PanelButton_showLock, false));

        mMarker = (ImageView) findViewById(R.id.marker_bottom);

        setLinePosition(a.getInt(R.styleable.PanelButton_linePosition, -1));

        a.recycle();
    }

    public void setListener(PanelButtonClick listener) {
        mListener = listener;
    }

    public void setHeader(String header) {
        if (mHeader != null) {
            mHeader.setText(header);
        }
    }

    public boolean isLocked() {
        return mLock != null && mLock.getVisibility() == VISIBLE;
    }

    public void setLock(boolean show) {
        if (mLock != null) {
            mLock.setVisibility(show ? VISIBLE : GONE);
        }
        if (mHeader != null) {
            mHeader.setAlpha(show ? .5f : 1f);
        }
        if (mContent != null) {
            mContent.setAlpha(show ? .5f : 1f);
        }
    }

    public void setPanelType(int type) {
        mType = type;
        switch (type) {
            case 2:
                setOnClickListener(null);
                break;

            default:
                setOnClickListener(this);
        }
    }

    public void setPanelText(@StringRes int id) {
        setPanelText(getResources().getString(id));
    }

    public void setPanelText(String text) {
        hideValues();
        if (mText != null) {
            mText.setText(text);
        }
    }

    public void setPanelImage(Drawable drawable) {
        hideValues();
        if (mImage != null) {
            mImage.setImageDrawable(drawable);
        }
    }

    public void setPanelSwitch(@StringRes int id) {
        setPanelSwitch(getResources().getString(id));
    }

    public void setPanelSwitch(String text) {
        hideValues();
        if (mSwitch != null) {
            mSwitch.setText(text);
        }
    }

    public void setPanelSwitch(boolean isChecked) {
        if (mSwitch != null) {
            mSwitch.setChecked(isChecked);
        }
    }

    public void setLinePosition(int position) {
        switch (position) {
            case 0:
                mLine = findViewById(R.id.line_left);
                break;

            case 1:
                mLine = findViewById(R.id.line_bottom);
                break;

            case 2:
                mLine = findViewById(R.id.line_right);
                break;

            default:
                // no-op
        }

        if (mLine != null) {
            mLine.setVisibility(VISIBLE);
        }
    }

    private void hideValues() {
        if (mText != null) {
            mText.setVisibility(GONE);
        }

        if (mImage != null) {
            mImage.setVisibility(GONE);
        }

        if (mSwitch != null) {
            mSwitch.setVisibility(GONE);
        }

        switch (mType) {
            case 0:
                if (mText != null) {
                    mText.setVisibility(VISIBLE);
                }
                break;

            case 1:
                if (mImage != null) {
                    mImage.setVisibility(VISIBLE);
                }
                break;

            case 2:
                if (mSwitch != null) {
                    mSwitch.setVisibility(VISIBLE);
                }
        }
    }

    protected void showChangeDialog(DialogInterface.OnClickListener positiveButton) {
        showChangeDialog(positiveButton, null);
    }

    protected void showChangeDialog(DialogInterface.OnClickListener positiveButton, DialogInterface.OnClickListener negativeButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_AlertDialog);
        builder.setTitle(getContext().getString(R.string.change_title));
        builder.setMessage(getContext().getString(R.string.change_message));
        builder.setPositiveButton(getContext().getString(R.string.change_proceed), positiveButton);
        builder.setNegativeButton(getContext().getString(R.string.change_cancel), negativeButton);
        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void onClick(View v) {
        if (mLock != null && mLock.getVisibility() == VISIBLE && isNormal()) {
            if (mType == 2 && mSwitch != null) {
                mSwitch.setChecked(!mSwitch.isChecked());
            }
            showChangeDialog(this);
        } else {
            if (mType == 2) {
                if (mListener != null && mSwitch != null) {
                    mListener.onChangedSwitch(this, mSwitch.isChecked());
                }
            } else {
                if (mListener != null) {
                    mListener.onClick(this);
                }
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mType == 2) {
            if (mListener != null & mSwitch != null) {
                mSwitch.setChecked(!mSwitch.isChecked());
                mListener.onChangedSwitch(this, mSwitch.isChecked());
            }
        } else {
            if (mListener != null) {
                mListener.onClick(this);
            }
        }
    }

    public int toggleMarker() {
        int retValue = LEVEL_UNKNOWN;

        if (mMarker != null) {
            Drawable drawable = mMarker.getDrawable();
            if (drawable != null) {
                int level = drawable.getLevel();
                retValue = setMarkerLevel(LEVEL_TOGGLED - level);
            }
        }

        return retValue;
    }

    public int getMarkerLevel() {
        int retValue = LEVEL_UNKNOWN;

        if (mMarker != null) {
            Drawable drawable = mMarker.getDrawable();
            if (drawable != null) {
                retValue = drawable.getLevel();
            }
        }

        return retValue;
    }

    public int setMarkerLevel(int level) {
        int retValue = LEVEL_UNKNOWN;

        if (mLayer != null && mMarker != null) {
            Drawable drawable = mMarker.getDrawable();
            if (drawable != null) {
                drawable.setLevel(level);
                retValue = drawable.getLevel();
                switch (retValue) {
                    case LEVEL_TOGGLED:
                        mLayer.setBackgroundColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray_black_20));
                        break;

                    default:
                        mLayer.setBackgroundColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray));
                        break;
                }
            }
        }

        return retValue;
    }

    private boolean isNormal() {
        return (getMarkerLevel() == LEVEL_NORMAL);
    }

    public void disableToggle() {
        setClickable(false);
        mLayer.setBackgroundColor(getResources().getColor(R.color.constant_sap_yellow_1));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setClickable(true);
                setMarkerLevel(getMarkerLevel());
            }
        }, 200);
    }

    public interface PanelButtonClick {
        void onClick(PanelButton view);

        void onChangedSwitch(PanelButton view, boolean isChecked);
    }
}
