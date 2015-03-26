package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.RegattaFragment;
import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.ui.activities.AbstractRegattaActivity;

public class RegattaActivity extends AbstractRegattaActivity{
	
	private String leaderboardName;
	private String checkinDigest;
	private List<MarkInfo> marks;
	// private CheckinUrlInfo checkinUrl;
	
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

        // checkinUrl = DatabaseHelper.getInstance().getCheckinUrl(this, checkinDigest);
        // manager = new CheckinManager(checkinUrl.urlString, this);

        setMarks(DatabaseHelper.getInstance().getMarks(this, checkinDigest));

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
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            getSupportActionBar().setTitle(leaderboardName);
        }
        RegattaFragment regattaFragment = new RegattaFragment();
        replaceFragment(R.id.content_frame, regattaFragment);
    }
	

	@Override
	public void onCheckinDataAvailable(AbstractCheckinData arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getOptionsMenuResId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		leaderboardName = (String) getIntent().getExtras().get(getString(R.string.leaderboard_name));
		setTitle(leaderboardName);
	}


	public List<MarkInfo> getMarks() {
		return marks;
	}


	public void setMarks(List<MarkInfo> marks) {
		this.marks = marks;
	}

}
