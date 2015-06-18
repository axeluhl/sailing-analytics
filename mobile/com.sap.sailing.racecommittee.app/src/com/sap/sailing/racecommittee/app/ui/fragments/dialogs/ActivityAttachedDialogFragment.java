package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;


public abstract class ActivityAttachedDialogFragment extends AttachedDialogFragment {

    protected DialogListenerHost hostActivity;

    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);

        if (activity instanceof DialogListenerHost) {
            this.hostActivity = (DialogListenerHost) activity;
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Instance of %s must be attached to instances of %s. Tried to attach to %s.",
                            ActivityAttachedDialogFragment.class.getName(),
                            DialogListenerHost.class.getName(),
                            activity.getClass().getName()));
        }
    }

    @Override
    protected DialogListenerHost getHost() {
        return this.hostActivity;
    }

}
