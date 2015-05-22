package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite;

import android.app.Activity;
import android.os.Bundle;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public abstract class PrerequisiteRaceDialog<PrerequisiteType extends RacingProcedurePrerequisite, ChosenType> extends
        RaceDialogFragment {

    private static final String TAG = PrerequisiteRaceDialog.class.getName();

    private static final String EXTRA_IS_PREREQUISITE = "_EXTRA_IS_PREREQUISITE";

    public static RaceDialogFragment setNormalArguments(PrerequisiteRaceDialog<?, ?> dialog, ManagedRace race) {
        Bundle arguments = RaceDialogFragment.createArguments(race);
        arguments.putBoolean(EXTRA_IS_PREREQUISITE, false);
        dialog.setArguments(arguments);
        return dialog;
    }

    public static <T extends RacingProcedurePrerequisite> RaceDialogFragment setPrerequisiteArguments(
            PrerequisiteRaceDialog<T, ?> dialog, ManagedRace race, T prerequisite) {
        Bundle arguments = RaceDialogFragment.createArguments(race);
        arguments.putBoolean(EXTRA_IS_PREREQUISITE, true);
        dialog.setArguments(arguments);
        dialog.setPrerequisite(prerequisite);
        return dialog;
    }

    private PrerequisiteType prerequisite;

    private void setPrerequisite(PrerequisiteType prerequisite) {
        this.prerequisite = prerequisite;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (isInPrerequisiteMode()) {
            if (prerequisite == null) {
                ExLog.w(getActivity(), TAG, "Fragment not initialized correctly. Restored?");
                this.dismissAllowingStateLoss();
            }
            setCancelable(false);
        }
    }

    private boolean isInPrerequisiteMode() {
        Bundle arguments = getArguments();
        return arguments.containsKey(EXTRA_IS_PREREQUISITE) && arguments.getBoolean(EXTRA_IS_PREREQUISITE);
    }

    protected void onChosen(ChosenType value) {
        if (isInPrerequisiteMode()) {
            onPrerequisiteChosen(prerequisite, value);
        } else {
            onNormalChosen(value);
        }
    }

    protected abstract void onNormalChosen(ChosenType value);

    protected abstract void onPrerequisiteChosen(PrerequisiteType prerequisite, ChosenType value);

}
