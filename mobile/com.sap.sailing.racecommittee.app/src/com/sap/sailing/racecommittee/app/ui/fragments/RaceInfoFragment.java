package com.sap.sailing.racecommittee.app.ui.fragments;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RaceInfoFragment extends RaceFragment {

    private final static String TAG = RaceInfoFragment.class.getName();

    private RaceInfoFragmentChooser infoFragmentChooser;
    private RaceFragment infoFragment;

    public RaceInfoFragment() {
        this.infoFragmentChooser = null;
        this.infoFragment = null; // will be set later by switchToInfoFragment()
    }

    // TODO: Why is this needed if only used without arguments and even never called?
    public static RaceInfoFragment newInstance() {
        RaceInfoFragment fragment = new RaceInfoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_info_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        infoFragmentChooser = RaceInfoFragmentChooser.on(getRaceState().getRacingProcedure().getType());

        // Initial fragment selection...

        // TODO: why at all is the fragment managing other fragments. Shouldn't this be done by the containing activity?
        switchToInfoFragment();
    }

    public void onResume() {
        super.onResume();
        getRace().getState().addChangedListener(stateChangedListener);
        switchToInfoFragment();
    }

    @Override
    public void onStop() {
        // TODO: If the listener is added on resume it maybe should be removed onPause?!
        getRace().getState().removeChangedListener(stateChangedListener);
        super.onStop();
    }

    protected void switchToInfoFragment() {
        RaceFragment newInfoFragment = infoFragmentChooser.choose(getActivity(), getRace());
        if (infoFragment == null || !newInfoFragment.getClass().equals(infoFragment.getClass())) {
            switchToInfoFragment(newInfoFragment);
        }
    }

    protected void switchToInfoFragment(RaceFragment chosenFragment) {
        ExLog.i(getActivity(), TAG, String.format("Switched to %s fragment for race %s with status %s",
                chosenFragment.getClass().getName(), getRace().getId(), getRace().getStatus()));

        this.infoFragment = chosenFragment;
        displayInfoFragment();
    }

    private void displayInfoFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.infoContainer, infoFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private RaceStateChangedListener stateChangedListener = new BaseRaceStateChangedListener() {

        @Override
        public void onRacingProcedureChanged(ReadonlyRaceState state) {
            infoFragmentChooser = RaceInfoFragmentChooser.on(state.getRacingProcedure().getType());
            switchToInfoFragment();
        }

        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            switchToInfoFragment();
        }

        @Override
        public void onCourseDesignChanged(ReadonlyRaceState state) {
        }

        @Override
        public void onWindFixChanged(ReadonlyRaceState state) {
        }
    };
}
