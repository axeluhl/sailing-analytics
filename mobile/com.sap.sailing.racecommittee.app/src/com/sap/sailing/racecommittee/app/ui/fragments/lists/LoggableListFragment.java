package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.android.shared.logging.LifecycleLogger;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public abstract class LoggableListFragment extends ListFragment {

    private LifecycleLogger lifeLogger;
    private View lastSelected;

    public LoggableListFragment() {
        this.lifeLogger = new LifecycleLogger();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifeLogger.onCreate(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        lifeLogger.onStart(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        lifeLogger.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        lifeLogger.onPause(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        lifeLogger.onStop(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifeLogger.onDestroy(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Fragment fragment = getTargetFragment();
        if (fragment != null && !fragment.isAdded()) {
            setTargetFragment(null, -1);
        }

        super.onSaveInstanceState(outState);
    }

    protected void setStyleClicked(View view) {
        TextView textView;
        ImageView imageView;

        // reset last styles:
        if (lastSelected != null) {
            textView = (TextView) lastSelected.findViewById(R.id.list_item_subtitle);
            if (textView != null) {
                textView.setTextColor(ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray));
            }

            imageView = (ImageView) lastSelected.findViewById(R.id.checked);
            if (imageView != null) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

        // set new styles
        textView = (TextView) view.findViewById(R.id.list_item_subtitle);
        if (textView != null) {
            textView.setTextColor(ThemeHelper.getColor(getActivity(), R.attr.white));
        }
        imageView = (ImageView) view.findViewById(R.id.checked);
        if (imageView != null) {
            imageView.setVisibility(View.VISIBLE);
        }

        lastSelected = view;
    }
}
