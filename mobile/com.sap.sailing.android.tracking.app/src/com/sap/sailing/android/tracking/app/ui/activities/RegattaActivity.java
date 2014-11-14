package com.sap.sailing.android.tracking.app.ui.activities;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.MenuItem;

public class RegattaActivity extends BaseActivity {

    private final static String TAG = RegattaActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        
        String regattaName = intent.getStringExtra(getString(R.string.regatta_name));
        String eventName 	= intent.getStringExtra(getString(R.string.event_name));

        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(regattaName);
            Spannable subtitle = new SpannableString("Registered for: " + eventName);
            StyleSpan styleBold = new StyleSpan(Typeface.BOLD);
            subtitle.setSpan(styleBold, 16, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setSubtitle(subtitle);
        }

        replaceFragment(R.id.content_frame, new RegattaFragment());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
