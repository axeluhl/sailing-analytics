package com.sap.sailing.racecommittee.app.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.data.InMemoryDataStore;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter.JuryFlagClickedListener;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;
import com.sap.sailing.racecommittee.app.ui.comparators.BoatClassSeriesDataFleetComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.ManagedRaceStartTimeComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.RaceListDataTypeComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ProtestTimeDialogFragment;
import com.sap.sailing.racecommittee.app.utils.ColorHelper;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

import java.io.Serializable;
import java.util.*;

public class RaceListFragment extends LoggableFragment implements OnItemClickListener, JuryFlagClickedListener,
        OnItemSelectedListener, TickListener, OnScrollListener {

    private final static String TAG = RaceListFragment.class.getName();
    private final static String LAYOUT = "layout";
    private final static String HEADER = "header";
    private ManagedRaceListAdapter mAdapter;
    private RaceListCallbacks mCallbacks;
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
        }

        public void update(ReadonlyRaceState state) {
            dataChanged(state);
            filterChanged();
        }
    };
    public RaceListFragment() {
        mFilterMode = FilterMode.ACTIVE;
        mSelectedRace = null;
        mManagedRacesById = new HashMap<>();
        mRacesByGroup = new TreeMap<>(new BoatClassSeriesDataFleetComparator());
        mViewItems = new ArrayList<>();
    }

    public static RaceListFragment newInstance(int layout) {
        RaceListFragment fragment = new RaceListFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT, layout);
        fragment.setArguments(args);
        return fragment;
    }

    public static RaceListFragment newInstance(int layout, SpannableString header) {
        RaceListFragment fragment = newInstance(layout);
        Bundle args = fragment.getArguments();
        args.putString(HEADER, header.toString());
        return fragment;
    }

    private void dataChanged(ReadonlyRaceState changedState) {
        List<RaceListDataType> adapterItems = mAdapter.getItems();
        for (RaceListDataType adapterItem : adapterItems) {
            if (adapterItem instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceView = (RaceListDataTypeRace) adapterItem;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void filterChanged() {
        mAdapter.sort(new RaceListDataTypeComparator());
        mAdapter.getFilter().filterByMode(getFilterMode());
        mAdapter.notifyDataSetChanged();

        if (mCurrent != null && mAll != null) {
            int colorGrey = ColorHelper.getThemedColor(getActivity(), R.attr.sap_light_gray);
            int colorOrange = ColorHelper.getThemedColor(getActivity(), R.attr.sap_yellow_1);
            mCurrent.setTextColor(colorGrey);
            mAll.setTextColor(colorGrey);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mCurrent.setBackground(null);
                mAll.setBackground(null);
            } else {
                mCurrent.setBackgroundDrawable(null);
                mAll.setBackgroundDrawable(null);
            }

            int id;
            if (AppPreferences.on(getActivity()).getTheme().equals(AppConstants.LIGHT_THEME)) {
                id = R.drawable.nav_drawer_tab_button_light;
            } else {
                id = R.drawable.nav_drawer_tab_button_dark;
            }
            Drawable drawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = getActivity().getDrawable(id);
            } else {
                drawable = getResources().getDrawable(id);
            }
            switch (getFilterMode()) {
                case ALL:
                    mAll.setTextColor(colorOrange);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mAll.setBackground(drawable);
                    } else {
                        mAll.setBackgroundDrawable(drawable);
                    }
                    break;

                default:
                    mCurrent.setTextColor(colorOrange);
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

    public void setFilterMode(FilterMode filterMode) {
        mFilterMode = filterMode;
        filterChanged();
    }

    @Override
    public void notifyTick() {
        if (mAdapter != null && mAdapter.getCount() >= 0 && mUpdateList) {
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
            mCallbacks = (RaceListCallbacks) activity;
        } catch (ClassCastException ex) {
            ExLog.ex(getActivity(), TAG, ex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout = R.layout.race_list_vertical;
        if (getArguments() != null && getArguments().getInt(LAYOUT) != 0) {
            layout = getArguments().getInt(LAYOUT);
        }
        View view = inflater.inflate(layout, container, false);

        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnScrollListener(this);

        mCurrent = (Button) view.findViewById(R.id.races_current);
        if (mCurrent != null) {
            mCurrent.setTypeface(Typeface.DEFAULT_BOLD);
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
            mAll.setTypeface(Typeface.DEFAULT_BOLD);
            mAll.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    setFilterMode(FilterMode.ALL);
                    filterChanged();
                }
            });
        }

        mHeader = (TextView) view.findViewById(R.id.regatta_data);
        if (getArguments() != null && getArguments().getString(HEADER) != null) {
            mHeader.setText(getArguments().getString(HEADER));
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.nav_button);
        if (imageView != null) {
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    InMemoryDataStore.INSTANCE.reset();
                    ((RacingActivity) getActivity()).logout();
                }
            });
        }

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
        mAdapter.setSelectedRace(mAdapter.getItem(position));
        mCallbacks.onRaceListItemSelected(mAdapter.getItem(position));
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

    public void setUp(DrawerLayout drawerLayout, SpannableString header) {
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setStatusBarBackgroundColor(ColorHelper.getThemedColor(getActivity(), R.attr.colorPrimaryDark));
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
        List<ManagedRace> raceList = new ArrayList<>(races);
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

    public void openDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    private void unregisterOnAllRaces() {
        for (ManagedRace managedRace : mManagedRacesById.values()) {
            managedRace.getState().removeChangedListener(stateListener);
        }
    }

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

    public static interface RaceListCallbacks {
        void onRaceListItemSelected(RaceListDataType selectedItem);
    }
}
