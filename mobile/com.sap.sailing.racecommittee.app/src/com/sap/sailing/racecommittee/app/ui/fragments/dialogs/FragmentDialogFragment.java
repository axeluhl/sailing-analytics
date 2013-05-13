package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.Fragment;

public abstract class FragmentDialogFragment extends BaseDialogFragment {

    @Override
    protected DialogFragmentButtonListener getHost() {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment instanceof DialogFragmentButtonListener) {
            return (DialogFragmentButtonListener) targetFragment;
        }
        throw new IllegalStateException(
                String.format(
                        "Instance of %s must be attached to instances of %s.",
                        FragmentDialogFragment.class.getName(),
                        DialogFragmentButtonListener.class.getName()));
    }

}
