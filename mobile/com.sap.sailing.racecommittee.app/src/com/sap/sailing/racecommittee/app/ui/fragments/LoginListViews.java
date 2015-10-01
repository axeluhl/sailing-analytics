package com.sap.sailing.racecommittee.app.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ScreenHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoggableDialogFragment;

public class LoginListViews extends LoggableDialogFragment implements View.OnClickListener {

    private Button mSignUp;
    private IntentListener mListener;

    private ToggleContainer mEventContainer;
    private ToggleContainer mAreaContainer;
    private ToggleContainer mPositionContainer;

    public LoginListViews() {
        mListener = new IntentListener();
    }

    public ToggleContainer getEventContainer() {
        return mEventContainer;
    }

    public ToggleContainer getAreaContainer() {
        return mAreaContainer;
    }

    public ToggleContainer getPositionContainer() {
        return mPositionContainer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_listviews, container, false);

        View event_layout = ViewHelper.get(view, R.id.events);
        FrameLayout event_fragment = ViewHelper.get(view, R.id.event_fragment);
        RelativeLayout event_header = ViewHelper.get(view, R.id.event_header);
        TextView event_text = ViewHelper.get(view, R.id.selected_event);
        mEventContainer = new ToggleContainer(view, event_fragment, event_header, event_text, null);

        View area_layout = ViewHelper.get(view, R.id.areas);
        FrameLayout area_fragment = ViewHelper.get(view, R.id.area_fragment);
        RelativeLayout area_header = ViewHelper.get(view, R.id.area_header);
        TextView area_text = ViewHelper.get(view, R.id.selected_area);
        ArrayList<View> above_area = new ArrayList<>();
        above_area.add(event_layout);
        mAreaContainer = new ToggleContainer(view, area_fragment, area_header, area_text, above_area);

        FrameLayout position_fragment = ViewHelper.get(view, R.id.position_fragment);
        RelativeLayout position_header = ViewHelper.get(view, R.id.position_header);
        TextView position_text = ViewHelper.get(view, R.id.selected_position);
        ArrayList<View> above_position = new ArrayList<>();
        above_position.add(event_layout);
        above_position.add(area_layout);
        mPositionContainer = new ToggleContainer(view, position_fragment, position_header, position_text, above_position);

        // add listeners to the click areas
        if (event_header != null) {
            event_header.setOnClickListener(this);
        }
        if (area_header != null) {
            area_header.setOnClickListener(this);
        }
        if (position_header != null) {
            position_header.setOnClickListener(this);
        }

        mSignUp = (Button) view.findViewById(R.id.login_submit);
        if (mSignUp != null) {
            mSignUp.setOnClickListener(this);
        }

//        onClick(event_header);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mListener, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mListener);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.event_header:
                mAreaContainer.close();
                mPositionContainer.close();
                mEventContainer.toggle();
                break;

            case R.id.area_header:
                mEventContainer.close();
                mPositionContainer.close();
                mAreaContainer.toggle();
                break;

            case R.id.position_header:
                mEventContainer.close();
                mAreaContainer.close();
                mPositionContainer.toggle();
                break;

            default:
                break;
        }

        showButton();
    }

    public void closeAll() {
        mEventContainer.close();
        mAreaContainer.close();
        mPositionContainer.close();
        showButton();
    }

    private void showButton() {
        mSignUp.setVisibility(View.GONE);
        if (mEventContainer.isClosed() && mAreaContainer.isClosed() && mPositionContainer.isClosed()) {
            mSignUp.setVisibility(View.VISIBLE);
        }
    }

    public class ToggleContainer {
        private View mRootView;
        private FrameLayout mFrame;
        private TextView mText;
        private RelativeLayout mHeader;
        private List<View> mLayouts;

        public ToggleContainer(View rootView, FrameLayout frame, RelativeLayout header, TextView text, List<View> layouts) {
            mRootView = rootView;
            mFrame = frame;
            mText = text;
            mHeader = header;
            mLayouts = layouts;
        }

        public void toggle() {
            final int[] pos = new int[2];

            if (mFrame != null && mFrame.getLayoutParams() != null) {
                // open the frame
                if (mFrame.getLayoutParams().height == 0) {
                    mFrame.getLocationOnScreen(pos);
                    if (AppUtils.with(getActivity()).isPhoneLand()) {
                        if (mLayouts != null) {
                            for (View view : mLayouts) {
                                setVisibility(view, View.GONE);
                            }
                        }
                        mRootView.getLocationOnScreen(pos);
                        pos[1] += mHeader.getMeasuredHeight();
                    }
                    mFrame.getLayoutParams().height = ScreenHelper.on(getActivity()).getScreenHeight() - pos[1];
                    mFrame.requestLayout();
                    setVisibility(mText, View.GONE);
                } else {
                    close();
                }
            }
        }

        public void close() {
            if (mFrame != null && mFrame.getLayoutParams() != null) {
                if (!AppUtils.with(getActivity()).is10inch() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (mLayouts != null) {
                        for (View view : mLayouts) {
                            setVisibility(view, View.VISIBLE);
                        }
                    }
                }
                mFrame.getLayoutParams().height = 0;
                mFrame.requestLayout();
                setVisibility(mText, View.VISIBLE);
            }
        }

        public boolean isClosed() {
            return (mFrame != null && mFrame.getLayoutParams() != null && mFrame.getLayoutParams().height == 0);
        }

        public RelativeLayout getHeader() {
            return this.mHeader;
        }

        public void setHeaderText(String header) {
            mText.setText(header);
        }

        private void setVisibility(View view, int visibility) {
            if (view != null) {
                view.setVisibility(visibility);
            }
        }
    }

    private class IntentListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AppConstants.INTENT_ACTION_TOGGLE:
                    String data = intent.getExtras().getString(AppConstants.INTENT_ACTION_EXTRA);
                    if (AppConstants.INTENT_ACTION_TOGGLE_EVENT.equals(data)) {
                        onClick(mEventContainer.getHeader());
                    }
                    if (AppConstants.INTENT_ACTION_TOGGLE_AREA.equals(data)) {
                        onClick(mAreaContainer.getHeader());
                    }
                    if (AppConstants.INTENT_ACTION_TOGGLE_POSITION.equals(data)) {
                        onClick(mPositionContainer.getHeader());
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
