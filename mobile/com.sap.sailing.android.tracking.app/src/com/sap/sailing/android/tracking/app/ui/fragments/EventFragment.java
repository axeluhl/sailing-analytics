package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;

public class EventFragment extends BaseFragment {

    private static final String EVENT_INFO = "event.info";

    private WebView mWebView;

    public static EventFragment newInstance(EventInfo eventInfo) {
        Bundle args = new Bundle();
        args.putParcelable(EVENT_INFO, eventInfo);
        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventInfo eventInfo = getArguments().getParcelable(EVENT_INFO);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), description, Toast.LENGTH_SHORT).show();
            }
        });

        String url = getEventURL(eventInfo);
        mWebView.loadUrl(url);

    }

    private String getEventURL(EventInfo eventInfo) {
        AppPreferences preferences = new AppPreferences(getActivity());
        return eventInfo.server + preferences.getServerEventUrl(eventInfo.id);
    }
}
