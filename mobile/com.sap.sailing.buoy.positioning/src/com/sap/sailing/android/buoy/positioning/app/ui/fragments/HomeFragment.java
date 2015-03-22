package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.adapter.RegattaAdapter;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract;
import com.sap.sailing.android.buoy.positioning.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.buoy.positioning.app.util.CheckinManager;
import com.sap.sailing.android.shared.data.AbstractCheckinData;
import com.sap.sailing.android.shared.ui.activities.CheckinDataActivity;
import com.sap.sailing.android.ui.fragments.AbstractHomeFragment;

public class HomeFragment extends AbstractHomeFragment implements LoaderCallbacks<Cursor> {

	@SuppressWarnings("unused")
	private final static String TAG = HomeFragment.class.getName();

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		prefs = new AppPreferences(getActivity());

		ListView listView = (ListView) view.findViewById(R.id.listRegatta);
		if (listView != null) {
			listView.addHeaderView(inflater.inflate(
					R.layout.regatta_listview_header, null));

			adapter = new RegattaAdapter(getActivity(),
					R.layout.ragatta_listview_row, null, 0);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new ItemClickListener());
		}

		getLoaderManager().initLoader(REGATTA_LOADER, null, this);

		return view;
	}
	
	@Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(REGATTA_LOADER, null, this);

        String lastQRCode = prefs.getLastScannedQRCode();
        if (lastQRCode != null) {
            handleQRCode(lastQRCode);
        }
    }

	@Override
	public void displayUserConfirmationScreen(AbstractCheckinData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleScannedOrUrlMatchedUri(Uri uri) {
		CheckinManager manager = new CheckinManager(uri.toString(), (CheckinDataActivity) getActivity());
		manager.callServerAndGenerateCheckinData();

	}
	
	/**
     * Start regatta activity.
     * 
     * @param checkinDigest
     */
    private void startRegatta(String checkinDigest) {
        Intent intent = new Intent(getActivity(), RegattaActivity.class);
        intent.putExtra(getString(R.string.checkin_digest), checkinDigest);
        getActivity().startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
        case REGATTA_LOADER:
        	// TODO: FIX projection String
            String[] projection = new String[] { "events.event_checkin_digest", "events.event_id", "events._id",
                    "events.event_name", "events.event_server", "competitors.competitor_display_name",
                    "competitors.competitor_id", "leaderboards.leaderboard_name",
                    "competitors.competitor_country_code", "competitors.competitor_sail_id" };
            return new CursorLoader(getActivity(), AnalyticsContract.LeaderboardsMarksJoined.CONTENT_URI,
                    projection, null, null, null);

        default:
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
        case REGATTA_LOADER:
            adapter.changeCursor(cursor);
            break;

        default:
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
        case REGATTA_LOADER:
            adapter.changeCursor(null);
            break;

        default:
            break;
        }
    }

    private class ItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position < 1) // tapped header
            {
                return;
            }

            // -1, because there's a header row
            Cursor cursor = (Cursor) adapter.getItem(position - 1);

            String checkinDigest = cursor.getString(cursor.getColumnIndex("event_checkin_digest"));
            startRegatta(checkinDigest);
        }
    }

}
