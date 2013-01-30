package com.sap.sailing.racecommittee.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A two-pane activity - saves the visibility state of the right pane.
 *
 */
public abstract class BaseActivity extends Activity {
	
	protected MenuItem menuItemLive;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		menuItemLive = menu.findItem(R.id.LiveIcon);*/
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		/*case R.id.SystemInfo:
			fadeActivity(InformationActivity.class);
			return true;
		case R.id.OptionsSettings:
			fadeActivity(SettingsActivity.class);
			return true;
		case R.id.LiveIcon:
			Toast.makeText(this, AppConstants.getURL(this), Toast.LENGTH_SHORT).show();
			return true;
		case R.id.WindLog:
			//fadeActivity(WindActivity.class);
			return false;*/
		case android.R.id.home:
			return onHomeClicked();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	protected boolean onHomeClicked() {
		return false;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		updateLiveIcon();
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateLiveIcon();
	}

	private void updateLiveIcon() {
		/*if (menuItemLive == null)
			return;
		
		ExLog.i("OptionsMenu", "Querying remote status.");
		if (AppConstants.getURL(this).indexOf("sapsailing.com") != -1) {
			ExLog.i("OptionsMenu", "It's remote.");
			menuItemLive.setIcon(R.drawable.ic_menu_share_sapsailing);
		} else {
			ExLog.i("OptionsMenu", "It's local.");
			menuItemLive.setIcon(R.drawable.ic_menu_share_local);
		}*/
	}
	
	protected void fadeActivity(Class<?> activity) {
		Intent intent = new Intent(getBaseContext(), activity);
		fadeActivity(intent);
	}

	protected void fadeActivity(Intent intent) {
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
