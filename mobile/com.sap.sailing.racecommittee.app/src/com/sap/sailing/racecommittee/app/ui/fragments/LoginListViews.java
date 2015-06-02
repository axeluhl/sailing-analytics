package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.LoginActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoggableDialogFragment;
import com.sap.sailing.racecommittee.app.utils.StringHelper;

public class LoginListViews extends LoggableDialogFragment implements View.OnClickListener {

    private LoginActivity activity;

    private Button sign_up;
    private IntentListener listener;

    private EventToggleContainer event_container;
    private AreaToggleContainer area_container;
    private PositionToggleContainer position_container;

    public LoginListViews() {
        listener = new IntentListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (LoginActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_listviews, container, false);

        RelativeLayout event_header = (RelativeLayout) view.findViewById(R.id.event_header);
        RelativeLayout area_header = (RelativeLayout) view.findViewById(R.id.area_header);
        RelativeLayout position_header = (RelativeLayout) view.findViewById(R.id.position_header);

        // create the toggle container instances
        this.event_container = new EventToggleContainer(
                (FrameLayout) view.findViewById(R.id.event_fragment),
                event_header,
                (TextView) view.findViewById(R.id.selected_event)
        );
        this.area_container = new AreaToggleContainer(
                (FrameLayout) view.findViewById(R.id.area_fragment),
                area_header,
                (TextView) view.findViewById(R.id.selected_area)
        );
        this.position_container = new PositionToggleContainer(
                (FrameLayout) view.findViewById(R.id.position_fragment),
                position_header,
                (TextView) view.findViewById(R.id.selected_position)
        );

        // add listeners to the click areas
        if (event_header != null) event_header.setOnClickListener(this);
        if (area_header != null) area_header.setOnClickListener(this);
        if (position_header != null) position_header.setOnClickListener(this);

        sign_up = (Button) view.findViewById(R.id.login_submit);
        if (sign_up != null) {
            sign_up.setOnClickListener(this);
        }

        onClick(event_header);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // reset all texts
        event_container.resetHeaderText();
        area_container.resetHeaderText();
        position_container.resetHeaderText();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(listener, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(listener);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
        case R.id.event_header:
            event_container.toggle();
            area_container.close();
            position_container.close();
            break;

        case R.id.area_header:
            event_container.close();
            area_container.toggle();
            position_container.close();
            break;

        case R.id.position_header:
            event_container.close();
            area_container.close();
            position_container.toggle();
            break;

        default:
            break;
        }

        showButton();
    }


    public void closeAll() {
        event_container.close();
        area_container.close();
        position_container.close();
        showButton();
    }

    private void showButton() {
        sign_up.setVisibility(View.GONE);
        if (event_container.isClosed() && area_container.isClosed() && position_container.isClosed()){
            sign_up.setVisibility(View.VISIBLE);
        }
    }

        private abstract class ToggleContainer {
            private FrameLayout frame;
            private TextView text;
            private RelativeLayout header;

            public ToggleContainer(FrameLayout frame, RelativeLayout header, TextView text) {
                this.frame = frame;
                this.text = text;
                this.header = header;
            }

            public void toggle(){
                final int[] pos = new int[2];

                // reset the header text
                if (text != null) {
                    text.setText(null);
                }

                if (frame != null && frame.getLayoutParams() != null) {
                    // open the frame
                    if (frame.getLayoutParams().height == 0) {
                        frame.getLocationOnScreen(pos);
                        frame.getLayoutParams().height = getScreenHeight() - pos[1];
                    } else {
                        close();
                    }
                    frame.requestLayout();
                }
            }

            public void close(){
                if (frame != null && frame.getLayoutParams() != null) {
                    frame.getLayoutParams().height = 0;
                    frame.requestLayout();
                    setHeaderText();
                }
            }

            public void resetHeaderText(){
                if (text != null) text.setText(null);
            }

            public boolean isClosed(){
                if (frame != null && frame.getLayoutParams() != null && frame.getLayoutParams().height == 0) {
                    return true;
                }else {
                    return false;
                }
            }

            public RelativeLayout getHeader(){
                return this.header;
            }

            private int getScreenHeight() {
                WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                return point.y;
            }

            public void setHeaderText(){}

        }

        private class EventToggleContainer extends ToggleContainer {
            public EventToggleContainer(FrameLayout frame, RelativeLayout header, TextView text) {
                super(frame, header, text);
            }

            public void setHeaderText(){
                super.text.setText(activity.getEventName());
            }
        }

        private class AreaToggleContainer extends ToggleContainer {
            public AreaToggleContainer(FrameLayout frame, RelativeLayout header, TextView text) {
                super(frame, header, text);
            }
            public void setHeaderText(){
                super.text.setText(activity.getCourseName());
            }
        }

        private class PositionToggleContainer extends ToggleContainer {
            public PositionToggleContainer(FrameLayout frame, RelativeLayout header, TextView text) {
                super(frame, header, text);
            }
            public void setHeaderText(){
                super.text.setText(StringHelper.on(activity).getAuthor(activity.getPositionName()));
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
                        onClick(event_container.getHeader());
                    }
                    if (AppConstants.INTENT_ACTION_TOGGLE_AREA.equals(data)) {
                        onClick(area_container.getHeader());
                    }
                    if (AppConstants.INTENT_ACTION_TOGGLE_POSITION.equals(data)) {
                        onClick(position_container.getHeader());
                    }
                    break;

                default:
                    break;
                }
            }
    }
}
