package com.sap.sailing.android.buoy.positioning.app.ui.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.BuoyFragment;
import com.sap.sailing.android.buoy.positioning.app.ui.fragments.BuoyFragment.pingListener;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;

public class PositioningActivity extends BaseActivity implements pingListener {

	private MarkInfo markInfo;
	private MarkPingInfo markPing;
	private LeaderboardInfo leaderBoard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container);
		Intent intent = getIntent();
		String markerID = intent.getExtras().getString(
				getString(R.string.mark_id));
		String checkinDigest = intent.getExtras().getString(
				getString(R.string.checkin_digest));
		List<MarkInfo> marks = DatabaseHelper.getInstance().getMarks(this,
				checkinDigest);
		setLeaderBoard(DatabaseHelper.getInstance().getLeaderboard(this,
				checkinDigest));
		for (MarkInfo mark : marks) {
			if (mark.getId().equals(markerID)) {
				setMarkInfo(mark);
				break;
			}
		}
		if (markInfo != null) {
			setPingFromDatabase(markerID);
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setBackgroundColor(getResources().getColor(
					R.color.colorPrimary));
		}
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
			toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
			toolbar.setPadding(20, 0, 0, 0);
			getSupportActionBar().setTitle(
					getString(R.string.title_activity_positioning));
		}
		BuoyFragment fragment = new BuoyFragment();
		fragment.setPingListener(this);
		replaceFragment(R.id.content_frame, fragment);

	}

	public void setPingFromDatabase(String markerID) {
		List<MarkPingInfo> markPings = DatabaseHelper.getInstance()
				.getMarkPings(this, markerID);
		if (!markPings.isEmpty()) {
			setMarkPing(markPings.get(0));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		BuoyFragment fragment = (BuoyFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		if (fragment != null) {
			fragment.setPingListener(this);
		}
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

	public LeaderboardInfo getLeaderBoard() {
		return leaderBoard;
	}

	public void setLeaderBoard(LeaderboardInfo leaderBoard) {
		this.leaderBoard = leaderBoard;
	}

	@Override
	public void updatePing() {
		setPingFromDatabase(markInfo.getId());
	}
}
