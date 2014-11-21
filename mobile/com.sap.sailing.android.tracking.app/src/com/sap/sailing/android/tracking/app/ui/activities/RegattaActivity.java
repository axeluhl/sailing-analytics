package com.sap.sailing.android.tracking.app.ui.activities;

import java.util.Locale;

import android.content.Intent;
import android.database.Cursor;
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
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.ui.fragments.RegattaFragment;

public class RegattaActivity extends BaseActivity {

    private final static String TAG = RegattaActivity.class.getName();
    
    private String eventId;
    private String competitorId;
    
    private String competitorName;
    private String competitorCountryCode;
    private String competitorSailId;
    
    private String eventName;
    private String eventImageUrl;
    
    private String leaderboardName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        
        leaderboardName = intent.getStringExtra(getString(R.string.leaderboard_name));
        eventId = intent.getStringExtra(getString(R.string.event_id));
        competitorId = intent.getStringExtra(getString(R.string.competitor_id));

        fetchData(eventId, competitorId);
        
        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(leaderboardName);
            Spannable subtitle = new SpannableString("Registered for: " + eventName);
            StyleSpan styleBold = new StyleSpan(Typeface.BOLD);
            subtitle.setSpan(styleBold, 16, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setSubtitle(subtitle);
        }

        replaceFragment(R.id.content_frame, new RegattaFragment());
    }
    
    private void fetchData(String eventId, String competitorId)
    {
    	Cursor cc = getContentResolver().query(Competitor.CONTENT_URI, null, "competitor_id = \"" + competitorId + "\"", null, null);
		if (cc.moveToFirst()) {
			competitorName = cc.getString(cc.getColumnIndex(Competitor.COMPETITOR_DISPLAY_NAME));
			competitorCountryCode = cc.getString(cc.getColumnIndex(Competitor.COMPETITOR_COUNTRY_CODE));
			competitorSailId = cc.getString(cc.getColumnIndex(Competitor.COMPETITOR_SAIL_ID));
        }
		
		cc.close();
		
		Cursor ec = getContentResolver().query(Event.CONTENT_URI, null, "event_id = \"" + eventId + "\"", null, null);
		if (ec.moveToFirst()) {
			eventName = ec.getString(ec.getColumnIndex(Event.EVENT_NAME));
			eventImageUrl = ec.getString(ec.getColumnIndex(Event.EVENT_IMAGE_URL));
        }
		
		ec.close();
    }

    @Override
    protected void onResume() {
        TextView competitorNameTextView = (TextView)findViewById(R.id.competitor_name);
        competitorNameTextView.setText(competitorName);
        
        TextView sailIdTextView = (TextView)findViewById(R.id.sail_id);
        sailIdTextView.setText(competitorSailId);
        
        
        ImageView flagImageView = (ImageView)findViewById(R.id.flag_image);
        //String flagStr = String.format("%s.png", countryCode);
        String uri = "@drawable/" + competitorCountryCode.toLowerCase(Locale.getDefault());
        
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        
		try {
			Drawable res = getResources().getDrawable(imageResource);
			flagImageView.setImageDrawable(res);
		} catch (Exception e) {
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
