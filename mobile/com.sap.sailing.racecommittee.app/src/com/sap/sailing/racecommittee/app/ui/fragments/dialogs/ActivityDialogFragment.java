package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;


public abstract class ActivityDialogFragment extends BaseDialogFragment {

	protected DialogFragmentButtonListener hostActivity;
	
	public void onAttach(android.app.Activity activity) {
		super.onAttach(activity);
		
		if (activity instanceof DialogFragmentButtonListener) {
			this.hostActivity = (DialogFragmentButtonListener) activity;
		} else {
			throw new IllegalStateException(
					String.format(
							"Instance of %s must be attached to instances of %s. Tried to attach to %s.",
							ActivityDialogFragment.class.getName(),
							DialogFragmentButtonListener.class.getName(),
							activity.getClass().getName()));
		}
	};

	@Override
	protected DialogFragmentButtonListener getHost() {
		return this.hostActivity;
	}

}
