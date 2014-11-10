package com.sap.sailing.android.tracking.app.ui.activities;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.HomeFragment;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class StartActivity extends BaseActivity {
    
    private final static String TAG = StartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_launcher);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        replaceFragment(R.id.content_frame, new HomeFragment());
    }
}
