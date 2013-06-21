package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.Fragment;

public abstract class FragmentAttachedDialogFragment extends BaseDialogFragment {

    @Override
    protected DialogFragmentButtonListener getListener() {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment instanceof DialogFragmentButtonListener) {
            return (DialogFragmentButtonListener) targetFragment;
        }
        throw new IllegalStateException(String.format("Instance of %s must be attached to instances of %s.",
                FragmentAttachedDialogFragment.class.getName(), DialogFragmentButtonListener.class.getName()));
    }

}
