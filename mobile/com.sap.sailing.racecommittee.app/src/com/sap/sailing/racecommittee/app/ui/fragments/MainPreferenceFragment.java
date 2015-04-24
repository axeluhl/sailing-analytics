package com.sap.sailing.racecommittee.app.ui.fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.R;

public class MainPreferenceFragment extends LoggableFragment implements OnClickListener {

    private static final String TAG = MainPreferenceFragment.class.getName();

    public MainPreferenceFragment() {
        // nothing
    }

    public static MainPreferenceFragment newInstance() {
        return new MainPreferenceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.preference_header, container, false);

        for (int i = 0; i < layout.getChildCount(); i++) {
            Button button = (Button) layout.getChildAt(i);
            if (button != null) {
                button.setOnClickListener(this);
            }
        }

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        if (view != null && view instanceof Button) {
            String tag = (String) view.getTag();
            if (tag != null) {
                try {
                    Fragment fragment = (Fragment) Class.forName(tag).newInstance();
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(((Button) view).getText());
                    getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                } catch (ClassNotFoundException ex) {
                    ExLog.ex(getActivity(), TAG, ex);
                } catch (java.lang.InstantiationException ex) {
                    ExLog.ex(getActivity(), TAG, ex);
                } catch (IllegalAccessException ex) {
                    ExLog.ex(getActivity(), TAG, ex);
                }
            }
        }
    }
}
