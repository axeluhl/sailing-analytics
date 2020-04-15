package com.sap.sailing.android.tracking.app.ui.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.LeaderboardWebViewActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class LeaderboardFragment extends BaseFragment {

    private static final String TAG = LeaderboardFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();

        WebView webview = (WebView) getActivity().findViewById(R.id.web_view);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), description, Toast.LENGTH_SHORT).show();
            }
        });

        try {
            String url = getLeaderboardURL();
            webview.loadUrl(url);
        } catch (UnsupportedEncodingException e) {
            LeaderboardWebViewActivity activity = (LeaderboardWebViewActivity) getActivity();
            activity.showErrorPopup(R.string.error, R.string.error_could_not_complete_operation_on_server_try_again);
            ExLog.e(getActivity(), TAG, "UnsupportedEncodingException when creating leaderboard-url: " + e);
        }
    }

    private String getLeaderboardURL() throws UnsupportedEncodingException {
        LeaderboardWebViewActivity activity = (LeaderboardWebViewActivity) getActivity();
        // TODO When using an event-based leaderboard place again, the event's ID can be obtained like this:
        // String eventId = URLEncoder.encode(activity.eventId, "UTF-8");
        String leaderboardName = URLEncoder.encode(activity.leaderboardName, "UTF-8");
        String serverUrl = activity.serverUrl;

        String formatString = "%s/gwt/Leaderboard.html?name=%s"
                + "&showRaceDetails=false&embedded=true&hideToolbar=true";
        return String.format(formatString, serverUrl, leaderboardName);
    }
}
