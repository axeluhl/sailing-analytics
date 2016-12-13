package com.sap.sailing.android.tracking.app.ui.fragments;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.LocationHelper;
import com.sap.sailing.android.shared.util.TimeUtils;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackingTimeFragment extends BaseFragment {

    private static final String SIS_TRACKING_TIMER = "trackingTimer";
    private static final String THREAD_NAME = "Tracking Timer";

    private static final int TIMER_SET = 0;
    private static final long TIMER_DELAY = Duration.ONE_SECOND.asMillis();

    private TextView timerView;
    private TextView noGPSView;

    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_time, container, false);
        timerView = (TextView) layout.findViewById(R.id.tracking_time_label);
        noGPSView = (TextView) layout.findViewById(R.id.wait_for_gps);

        if (savedInstanceState != null) {
            timerView.setText(savedInstanceState.getString(SIS_TRACKING_TIMER));
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (LocationHelper.isGPSEnabled(getActivity())) {
            noGPSView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        HandlerThread thread = new HandlerThread(THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        thread.start();
        handler = new TimerHandler(thread.getLooper(), getActivity(), timerView);
        handler.sendEmptyMessageDelayed(TIMER_SET, TIMER_DELAY);
    }

    @Override
    public void onStop() {
        super.onStop();

        handler.getLooper().quit();
        handler.removeMessages(TIMER_SET);
        handler = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SIS_TRACKING_TIMER, timerView.getText().toString());
    }

    private static class TimerHandler extends Handler {

        private AppPreferences prefs;
        private TextView timer;

        TimerHandler(Looper looper, Context context, TextView timer) {
            super(looper);
            this.prefs = new AppPreferences(context);
            this.timer = timer;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_SET:
                    final long trackingTimerStarted = prefs.getTrackingTimerStarted();
                    if (trackingTimerStarted > 0) {
                        final TimePoint now = MillisecondsTimePoint.now();
                        if (timer != null) {
                            timer.post(new Runnable() {
                                @Override
                                public void run() {
                                    timer.setText(TimeUtils.formatDurationSince(now.minus(trackingTimerStarted).asMillis()));
                                }
                            });
                        }
                    }
                    sendEmptyMessageDelayed(TIMER_SET, TIMER_DELAY);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
