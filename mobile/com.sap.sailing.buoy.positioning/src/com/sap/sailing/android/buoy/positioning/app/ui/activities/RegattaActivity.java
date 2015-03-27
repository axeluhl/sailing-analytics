package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sap.sailing.android.buoy.positioning.app.BuildConfig;
import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.RegattaFragment;
import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.buoy.positioning.app.util.CheckinManager;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.CheckinData;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.activities.AbstractRegattaActivity;
import com.sap.sailing.android.shared.ui.customviews.OpenSansToolbar;

import java.util.ArrayList;
import java.util.List;

public class RegattaActivity extends AbstractRegattaActivity {

    private String leaderboardName;
    private String checkinDigest;
    private List<MarkInfo> marks;
    private final String TAG = RegattaActivity.class.getName();
    private String checkinUrl;

    private AppPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferences(this);
        prefs.setLastScannedQRCode(null);
        Intent intent = getIntent();

        setMarks(new ArrayList<MarkInfo>());

        checkinDigest = intent.getStringExtra(getString(R.string.checkin_digest));
        leaderboardName = intent.getStringExtra(getString(R.string.leaderboard_name));

        checkinUrl = DatabaseHelper.getInstance().getCheckinUrl(this, checkinDigest).urlString;

        setMarks(DatabaseHelper.getInstance().getMarks(this, checkinDigest));

        setContentView(R.layout.fragment_container);
        OpenSansToolbar toolbar = (OpenSansToolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.hideSubtitle();
            toolbar.setTitleSize(20);
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            getSupportActionBar().setTitle(leaderboardName);
        }
        RegattaFragment regattaFragment = new RegattaFragment();
        replaceFragment(R.id.content_frame, regattaFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        leaderboardName = (String) getIntent().getExtras().get(getString(R.string.leaderboard_name));
        setTitle(leaderboardName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.regatta_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                ExLog.i(this, TAG, "Clicked REFRESH.");
                CheckinManager manager = new CheckinManager(checkinUrl, this);
                manager.callServerAndGenerateCheckinData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getOptionsMenuResId() {
        return R.menu.regatta_menu;
    }


    @Override
    public void onCheckinDataAvailable(AbstractCheckinData checkinData) {
        CheckinData data = (CheckinData) checkinData;
        try {
            DatabaseHelper.getInstance().deleteRegattaFromDatabase(this, data.checkinDigest);
            DatabaseHelper.getInstance().storeCheckinRow(this,
                    data.marks, data.getLeaderboard(),
                    data.getCheckinUrl(), data.pings);
            getRegattaFragment().getAdapter().notifyDataSetChanged();
        } catch (DatabaseHelper.GeneralDatabaseHelperException e) {
            ExLog.e(this, TAG,
                    "Batch insert failed: " + e.getMessage());
            displayDatabaseError();
            return;
        }

        if (BuildConfig.DEBUG) {
            ExLog.i(this, TAG,
                    "Batch-insert of checkinData completed.");
        }
    }

    public RegattaFragment getRegattaFragment() {
        return (RegattaFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }


    public List<MarkInfo> getMarks() {
        return marks;
    }


    public void setMarks(List<MarkInfo> marks) {
        this.marks = marks;
    }

}
