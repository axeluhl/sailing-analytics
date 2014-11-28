package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.SqlDebugHelper;

public class BaseActivity extends ActionBarActivity {
	private static final String TAG = SendingServiceAwareActivity.class
			.getName();
	
	protected AppPreferences prefs;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = new AppPreferences(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// TODO: Remove debug string
		SqlDebugHelper.dumpAllTablesToConsole(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.options_menu_settings:
			ExLog.i(this, TAG, "Clicked SETTINGS.");
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
//		case R.id.options_menu_info:
//			ExLog.i(this, TAG, "Clicked INFO.");
//			startActivity(new Intent(this, SystemInformationActivity.class));
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void replaceFragment(int view, Fragment fragment) {
		ExLog.i(this, TAG, "Set new Fragment: " + fragment.toString());

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(view, fragment);
		transaction.commit();
	}

	public void showProgressDialog(String title, String message) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(title);
		progressDialog.setMessage(message);
		progressDialog.show();
	}
	
	public void showProgressDialog(int string1Id, int string2Id)
	{
		showProgressDialog(getString(string1Id), getString(string2Id));
	}
	
	public void dismissProgressDialog()
	{
		if (progressDialog.isShowing())
		{
			progressDialog.dismiss();	
		}
	}

	public void showErrorPopup(String title, String message) {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).create();

		dialog.show();
	}
	
	public void showErrorPopup(int string1Id, int string2Id)
	{
		showErrorPopup(getString(string1Id), getString(string2Id));
	}
}
