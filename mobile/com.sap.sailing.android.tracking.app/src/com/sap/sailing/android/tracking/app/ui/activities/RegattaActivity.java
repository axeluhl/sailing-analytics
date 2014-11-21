package com.sap.sailing.android.tracking.app.ui.activities;

import java.util.Locale;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;

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
    protected void onResume() {
    	
    	Intent intent = getIntent();
    	
        String countryCode = intent.getStringExtra(getString(R.string.country_code));
        String competitorName = intent.getStringExtra(getString(R.string.competitor_name));
        String sailId = intent.getStringExtra(getString(R.string.sail_id));
        
        TextView competitorNameTextView = (TextView)findViewById(R.id.competitor_name);
        competitorNameTextView.setText(competitorName);
        
        TextView sailIdTextView = (TextView)findViewById(R.id.sail_id);
        sailIdTextView.setText(sailId);
        
        
        ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
        //String flagStr = String.format("%s.png", countryCode);
        String uri = "@drawable/" + countryCode.toLowerCase(Locale.getDefault());
        
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        
        try {
        Drawable res = getResources().getDrawable(imageResource);
        flagImageView.setImageDrawable(res);
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
        
    	super.onResume();
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
