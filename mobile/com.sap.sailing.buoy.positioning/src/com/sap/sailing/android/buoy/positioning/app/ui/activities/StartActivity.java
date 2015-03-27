package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.HomeFragment;
import com.sap.sailing.android.shared.ui.activities.AbstractStartActivity;
import com.sap.sailing.android.shared.ui.customviews.OpenSansToolbar;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;


public class StartActivity extends AbstractStartActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(getString(R.string.title_activity_start));
            OpenSansToolbar toolbar = (OpenSansToolbar) findViewById(R.id.toolbar);
            toolbar.hideSubtitle();
            toolbar.setTitleSize(20);
			getSupportActionBar().setTitle(getString(R.string.title_activity_start));
			getSupportActionBar().setHomeButtonEnabled(false);
        }
        replaceFragment(R.id.content_frame, new HomeFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getOptionsMenuResId() {
        return R.menu.empty_menu;
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
