package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.LeaderboardFragment;

public class LeaderboardWebViewActivity extends BaseActivity {
	
	public final static String LEADERBOARD_EXTRA_SERVER_URL = "leaderboardExtraServerUrl";
	public final static String LEADERBOARD_EXTRA_EVENT_ID = "leaderboardExtraEventId";
	public final static String LEADERBOARD_EXTRA_LEADERBOARD_NAME = "leaderboardExtraLeaderboardName";
	
	public String serverUrl;
	public String eventId;
	public String leaderboardName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		serverUrl = intent.getStringExtra(LEADERBOARD_EXTRA_SERVER_URL);
		eventId = intent.getStringExtra(LEADERBOARD_EXTRA_EVENT_ID);
		leaderboardName = intent.getStringExtra(LEADERBOARD_EXTRA_LEADERBOARD_NAME);
		
		setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            getSupportActionBar().setTitle(getString(R.string.title_activity_webview));
        }
		
		replaceFragment(R.id.content_frame, new LeaderboardFragment());	
	}
}
