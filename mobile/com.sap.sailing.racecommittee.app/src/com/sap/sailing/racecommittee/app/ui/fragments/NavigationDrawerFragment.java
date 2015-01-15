package com.sap.sailing.racecommittee.app.ui.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter.JuryFlagClickedListener;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;
import com.sap.sailing.racecommittee.app.ui.comparators.BoatClassSeriesDataFleetComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.ManagedRaceStartTimeComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.RaceListDataTypeComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ProtestTimeDialogFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public class NavigationDrawerFragment extends LoggableFragment implements OnItemClickListener, JuryFlagClickedListener,
        OnItemSelectedListener, TickListener, OnScrollListener {

    public enum FilterMode {
        ACTIVE(R.string.race_list_filter_show_active), ALL(R.string.race_list_filter_show_all);

        private String displayName;

        private FilterMode(int resId) {
            this.displayName = RaceApplication.getStringContext().getString(resId);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(RaceListDataType selectedItem);
    }

    private final static String TAG = NavigationDrawerFragment.class.getName();
    private ManagedRaceListAdapter mAdapter;
    private NavigationDrawerCallbacks mCallbacks;
    private Button mCurrent;
    private Button mAll;
    private TextView mHeader;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FilterMode mFilterMode;
    private ListView mListView;
    private HashMap<Serializable, ManagedRace> mManagedRacesById;
    private TreeMap<BoatClassSeriesFleet, List<ManagedRace>> mRacesByGroup;
    private ManagedRace mSelectedRace;
    private boolean mUpdateList;
    private ArrayList<RaceListDataType> mViewItems;

    private BaseRaceStateChangedListener stateListener = new BaseRaceStateChangedListener() {
        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            update(state);
        }

        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            update(state);
        };

        public void update(ReadonlyRaceState state) {
            dataChanged(state);
            filterChanged();
        };
    };

    public NavigationDrawerFragment() {
        mFilterMode = FilterMode.ACTIVE;
        mSelectedRace = null;
        mManagedRacesById = new HashMap<Serializable, ManagedRace>();
        mRacesByGroup = new TreeMap<BoatClassSeriesFleet, List<ManagedRace>>(new BoatClassSeriesDataFleetComparator());
        mViewItems = new ArrayList<RaceListDataType>();
    }

    private void dataChanged(ReadonlyRaceState changedState) {
        List<RaceListDataType> adapterItems = mAdapter.getItems();
        for (int i = 0; i < adapterItems.size(); ++i) {
            if (adapterItems.get(i) instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceView = (RaceListDataTypeRace) adapterItems.get(i);
                ManagedRace race = raceView.getRace();
                if (changedState != null && race.getState().equals(changedState)) {
                    boolean allowUpdateIndicator = !raceView.getRace().equals(mSelectedRace);
                    raceView.onStatusChanged(changedState.getStatus(), allowUpdateIndicator);
                }
            }
        }
        mAdapter.sort(new RaceListDataTypeComparator());
        mAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void filterChanged() {
        mAdapter.sort(new RaceListDataTypeComparator());
        mAdapter.getFilter().filterByMode(getFilterMode());
        mAdapter.notifyDataSetChanged();

        if (mCurrent != null && mAll != null) {
            mCurrent.setTextColor(getResources().getColor(R.color.grey_light));
            mAll.setTextColor(getResources().getColor(R.color.grey_light));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mCurrent.setBackground(null);
                mAll.setBackground(null);
            } else {
                mCurrent.setBackgroundDrawable(null);
                mAll.setBackgroundDrawable(null);
            }

            Drawable drawable = getResources().getDrawable(R.drawable.nav_drawer_tab_button);
            switch (getFilterMode()) {
            case ALL:
                mAll.setTextColor(getResources().getColor(R.color.white));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mAll.setBackground(drawable);
                } else {
                    mAll.setBackgroundDrawable(drawable);
                }
                break;

            default:
                mCurrent.setTextColor(getResources().getColor(R.color.white));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mCurrent.setBackground(drawable);
                } else {
                    mCurrent.setBackgroundDrawable(drawable);
                }
                break;
            }
        }
    }

    public FilterMode getFilterMode() {
        return mFilterMode;
    }

    @Override
    public void notifyTick() {
        if (mAdapter != null && mUpdateList) {
            ExLog.i(getActivity(), TAG, "notifyTick() on mAdapter called");
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RaceListDataTypeRace.initializeTemplates(this);

        if (mListView != null) {
            mAdapter = new ManagedRaceListAdapter(getActivity(), mViewItems, this);
            mListView.setAdapter(mAdapter);
            mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mListView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException ex) {
            ExLog.ex(getActivity(), TAG, ex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nav_drawer, container);

        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnScrollListener(this);

        mCurrent = (Button) view.findViewById(R.id.races_current);
        if (mCurrent != null) {
            mCurrent.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    setFilterMode(FilterMode.ACTIVE);
                    filterChanged();
                }
            });
        }

        mAll = (Button) view.findViewById(R.id.races_all);
        if (mAll != null) {
            mAll.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    setFilterMode(FilterMode.ALL);
                    filterChanged();
                }
            });
        }
        
        mHeader = (TextView) view.findViewById(R.id.regatta_data);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ExLog.i(getActivity(), TAG, "Touched " + mAdapter.getItem(position).toString());

        mDrawerLayout.closeDrawers();
        mCallbacks.onNavigationDrawerItemSelected(mAdapter.getItem(position));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setFilterMode(Arrays.asList(FilterMode.values()).get(position));
    }

    @Override
    public void onJuryFlagClicked(BoatClassSeriesFleet group) {
        if (mRacesByGroup.containsKey(group)) {
            List<ManagedRace> races = mRacesByGroup.get(group);
            ProtestTimeDialogFragment fragment = ProtestTimeDialogFragment.newInstace(races);
            fragment.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPause() {
        super.onPause();

        TickSingleton.INSTANCE.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        TickSingleton.INSTANCE.registerListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case SCROLL_STATE_FLING:
        case SCROLL_STATE_TOUCH_SCROLL:
            mUpdateList = false;
            break;

        default:
            mUpdateList = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        unregisterOnAllRaces();
        registerOnAllRaces();
        for (ManagedRace race : mManagedRacesById.values()) {
            stateListener.onStatusChanged(race.getState());
        }
    }

    @Override
    public void onStop() {
        unregisterOnAllRaces();
        super.onStop();
    }

    private void registerOnAllRaces() {
        for (ManagedRace managedRace : mManagedRacesById.values()) {
            managedRace.getState().addChangedListener(stateListener);
        }
    }

    public void setFilterMode(FilterMode filterMode) {
        mFilterMode = filterMode;
        filterChanged();
    }

    public void setUp(DrawerLayout drawerLayout, SpannableString header) {
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, (Toolbar) getActivity().findViewById(
                R.id.toolbar), R.string.nav_drawer_open, R.string.nav_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                mUpdateList = false;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                mUpdateList = true;
            }
        };

        mDrawerLayout.post(new Runnable() {

            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        if (mHeader != null) {
            mHeader.setText(header);
        }
    }

    public void setupOn(Collection<ManagedRace> races) {
        ExLog.i(getActivity(), TAG,
                String.format("Setting up %s with %d races.", this.getClass().getSimpleName(), races.size()));

        unregisterOnAllRaces();

        mManagedRacesById.clear();
        mViewItems.clear();
        List<ManagedRace> raceList = (List<ManagedRace>) races;
        Collections.sort(raceList, new ManagedRaceStartTimeComparator());
        for (ManagedRace managedRace : raceList) {
            mManagedRacesById.put(managedRace.getId(), managedRace);
            mViewItems.add(new RaceListDataTypeRace(managedRace));
        }

        registerOnAllRaces();

        // prepare views and do initial filtering
        filterChanged();
        mAdapter.sort(new RaceListDataTypeComparator());
        mAdapter.notifyDataSetChanged();
    }

    private void unregisterOnAllRaces() {
        for (ManagedRace managedRace : mManagedRacesById.values()) {
            managedRace.getState().removeChangedListener(stateListener);
        }
    }
}
