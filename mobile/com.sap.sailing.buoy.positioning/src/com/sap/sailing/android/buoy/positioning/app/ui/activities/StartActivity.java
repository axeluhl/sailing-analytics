package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.HomeFragment;
import com.sap.sailing.android.shared.ui.activities.AbstractStartActivity;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;


public class StartActivity extends AbstractStartActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(getString(R.string.title_activity_start));
			getSupportActionBar().setHomeButtonEnabled(false);
        }
        replaceFragment(R.id.content_frame, new HomeFragment());
    }
    
	@Override
	protected int getOptionsMenuResId() {
		return 0;
	}

	public AbstractHomeFragment getHomeFragment() {
		HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		return homeFragment;
	}
	
	public void startRegatta(String leaderboardName) {
		Intent intent = new Intent(this, RegattaActivity.class);
		intent.putExtra(getString(R.string.leaderboard_name), leaderboardName);
		startActivity(intent);
	}
}
