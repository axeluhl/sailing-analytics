package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.BuoyFragment;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;

public class PositioningActivity extends BaseActivity {
	
	private MarkInfo markInfo;
	private MarkPingInfo markPing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String markerID = intent.getExtras().getString(getString(R.string.mark_id));
		String checkinDigest = intent.getExtras().getString(getString(R.string.checkin_digest));
		List<MarkInfo> marks = DatabaseHelper.getInstance().getMarks(this, checkinDigest);
		for(MarkInfo mark : marks)
		{
			if(mark.getId().equals(markerID))
			{
				markInfo = mark;
				return;
			}
		}
		if(markInfo != null)
		{
			List<MarkPingInfo> markPings = DatabaseHelper.getInstance().getMarkPings(this, markerID);
			if(!markPings.isEmpty())
			{
				setMarkPing(markPings.get(0));
			}
		}
		setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
		replaceFragment(R.id.content_frame, new BuoyFragment());
		if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            getSupportActionBar().setTitle(getString(R.string.title_activity_positioning));
        }
	}
	
	@Override
    public void onResume() {
        super.onResume();
	}

	public MarkInfo getMarkInfo() {
		return markInfo;
	}

	public void setMarkInfo(MarkInfo markInfo) {
		this.markInfo = markInfo;
	}

	public MarkPingInfo getMarkPing() {
		return markPing;
	}

	public void setMarkPing(MarkPingInfo markPing) {
		this.markPing = markPing;
	}
}
