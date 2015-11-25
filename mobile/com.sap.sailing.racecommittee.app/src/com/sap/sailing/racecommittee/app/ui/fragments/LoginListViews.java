package com.sap.sailing.racecommittee.app.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

    public ToggleContainer getEventContainer(){ return mEventContainer; }
    public ToggleContainer getAreaContainer(){ return mAreaContainer; }
    public ToggleContainer getPositionContainer(){ return mPositionContainer; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_listviews, container, false);

        RelativeLayout event_header = (RelativeLayout) view.findViewById(R.id.event_header);
        RelativeLayout area_header = (RelativeLayout) view.findViewById(R.id.area_header);
        RelativeLayout position_header = (RelativeLayout) view.findViewById(R.id.position_header);

        // create the toggle container instances
        this.mEventContainer = new ToggleContainer((FrameLayout) view.findViewById(R.id.event_fragment), event_header, (TextView) view
            .findViewById(R.id.selected_event));
        this.mAreaContainer = new ToggleContainer((FrameLayout) view.findViewById(R.id.area_fragment), area_header, (TextView) view
            .findViewById(R.id.selected_area));
        this.mPositionContainer = new ToggleContainer((FrameLayout) view
            .findViewById(R.id.position_fragment), position_header, (TextView) view.findViewById(R.id.selected_position));

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

        onClick(event_header);

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
            mEventContainer.toggle();
            mAreaContainer.close();
            mPositionContainer.close();
            break;

        case R.id.area_header:
            mEventContainer.close();
            mAreaContainer.toggle();
            mPositionContainer.close();
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
        private FrameLayout frame;
        private TextView text;
        private RelativeLayout header;

        public ToggleContainer(FrameLayout frame, RelativeLayout header, TextView text) {
            this.frame = frame;
            this.text = text;
            this.header = header;
        }

        public void toggle() {
            final int[] pos = new int[2];

            if (frame != null && frame.getLayoutParams() != null) {
                // open the frame
                if (frame.getLayoutParams().height == 0) {
                    frame.getLocationOnScreen(pos);
                    frame.getLayoutParams().height = getScreenHeight() - pos[1];
                    text.setVisibility(View.GONE);
                } else {
                    close();
                }
                frame.requestLayout();
            }
        }

        public void close() {
            if (frame != null && frame.getLayoutParams() != null) {
                frame.getLayoutParams().height = 0;
                frame.requestLayout();
                text.setVisibility(View.VISIBLE);
            }
        }


        public boolean isClosed() {
            return (frame != null && frame.getLayoutParams() != null && frame.getLayoutParams().height == 0);
        }

        public RelativeLayout getHeader() {
            return this.header;
        }

        private int getScreenHeight() {
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            return point.y;
        }

        public void setHeaderText(String header) {
            text.setText(header);
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
