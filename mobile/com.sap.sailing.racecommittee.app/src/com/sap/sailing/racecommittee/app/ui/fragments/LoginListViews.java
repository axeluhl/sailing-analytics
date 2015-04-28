package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
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
import com.sap.sailing.racecommittee.app.ui.activities.LoginActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.LoggableDialogFragment;

public class LoginListViews extends LoggableDialogFragment implements View.OnClickListener {

    private LoginActivity activity;
    private RelativeLayout event_header;
    private FrameLayout event_listView;
    private FrameLayout area_listView;
    private FrameLayout position_listView;
    private TextView event_selected;
    private TextView area_selected;
    private TextView position_selected;
    private Button sign_up;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (LoginActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_listviews, container, false);

        // all click areas
        event_header = (RelativeLayout) view.findViewById(R.id.event_header);
        if (event_header != null) {
            event_header.setOnClickListener(this);
        }
        RelativeLayout area_header = (RelativeLayout) view.findViewById(R.id.area_header);
        if (area_header != null) {
            area_header.setOnClickListener(this);
        }
        RelativeLayout position_header = (RelativeLayout) view.findViewById(R.id.position_header);
        if (position_header != null) {
            position_header.setOnClickListener(this);
        }
        sign_up = (Button) view.findViewById(R.id.login_submit);
        if (sign_up != null) {
            sign_up.setOnClickListener(this);
        }

        // all listViews
        event_listView = (FrameLayout) view.findViewById(R.id.event_fragment);
        area_listView = (FrameLayout) view.findViewById(R.id.area_fragment);
        position_listView = (FrameLayout) view.findViewById(R.id.position_fragment);

        // all selected textViews
        event_selected = (TextView) view.findViewById(R.id.selected_event);
        area_selected = (TextView) view.findViewById(R.id.selected_area);
        position_selected = (TextView) view.findViewById(R.id.selected_position);

        onClick(event_header);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (event_selected != null) {
            event_selected.setText(null);
        }

        if (area_selected != null) {
            area_selected.setText(null);
        }

        if (position_selected != null) {
            position_selected.setText(null);
        }
    }

    @Override
    public void onClick(View view) {
        final int[] pos = new int[2];
        switch (view.getId()) {
            case R.id.event_header:
                if (event_selected != null) {
                    event_selected.setText(null);
                }
                if (event_listView != null && event_listView.getLayoutParams() != null) {
                    if (event_listView.getLayoutParams().height == 0) {
                        event_listView.getLocationOnScreen(pos);
                        event_listView.getLayoutParams().height = getScreenHeight() - pos[1];
                    } else {
                        closeEvent();
                    }
                    event_listView.requestLayout();
                }
                closeArea();
                closePosition();
                break;

            case R.id.area_header:
                closeEvent();
                if (area_selected != null) {
                    area_selected.setText(null);
                }
                if (area_listView != null && area_listView.getLayoutParams() != null) {
                    if (area_listView.getLayoutParams().height == 0) {
                        area_listView.getLocationOnScreen(pos);
                        area_listView.getLayoutParams().height = getScreenHeight() - pos[1];
                    } else {
                        closeArea();
                    }
                    area_listView.requestLayout();
                }
                closePosition();
                break;

            case R.id.position_header:
                closeEvent();
                closeArea();
                if (position_selected != null) {
                    position_selected.setText(null);
                }
                if (position_listView != null && position_listView.getLayoutParams() != null) {
                    if (position_listView.getLayoutParams().height == 0) {
                        position_listView.getLocationOnScreen(pos);
                        position_listView.getLayoutParams().height = getScreenHeight() - pos[1];
                    } else {
                        closePosition();
                    }
                    position_listView.requestLayout();
                }
                break;

            default:
                break;
        }

        showButton();
    }

    public void closeAll() {
        closeEvent();
        closeArea();
        closePosition();
        showButton();
    }

    private void closeEvent() {
        if (event_listView != null && event_listView.getLayoutParams() != null) {
            event_listView.getLayoutParams().height = 0;
            event_listView.requestLayout();
            if (activity != null) {
                event_selected.setText(activity.getEventName());
            }
        }
    }

    private void closeArea() {
        if (area_listView != null && area_listView.getLayoutParams() != null) {
            area_listView.getLayoutParams().height = 0;
            area_listView.requestLayout();
            if (activity != null) {
                area_selected.setText(activity.getCourseName());
            }
        }
    }

    private void closePosition() {
        if (position_listView != null && position_listView.getLayoutParams() != null) {
            position_listView.getLayoutParams().height = 0;
            position_listView.requestLayout();
            if (activity != null) {
                position_selected.setText(activity.getPositionName());
            }
        }
    }

    private void showButton() {
        sign_up.setVisibility(View.GONE);
        if (event_listView != null && event_listView.getLayoutParams() != null && event_listView.getLayoutParams().height == 0) {
            if (area_listView != null && area_listView.getLayoutParams() != null && area_listView.getLayoutParams().height == 0) {
                if (position_listView != null && position_listView.getLayoutParams() != null && position_listView.getLayoutParams().height == 0) {
                    sign_up.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }
}
