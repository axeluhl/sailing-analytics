package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.logging.ExLog;

public abstract class BaseDialogFragment extends DialogFragment {
	private final static String TAG = BaseDialogFragment.class.getName();
	
	protected abstract CharSequence getNegativeButtonLabel();
	protected abstract CharSequence getPositiveButtonLabel();
	protected abstract Builder createDialog(AlertDialog.Builder builder);
	
	private DialogFragmentHost host;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof DialogFragmentHost) {
			this.host = (DialogFragmentHost) activity;
		} else {
			ExLog.e(TAG, "Tried to attach tialog fragment to a non-host activity.");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return createDialog(new Builder(getActivity())
			.setNegativeButton(getNegativeButtonLabel(), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					onNegativeButton();
				}
			})
			.setPositiveButton(getPositiveButtonLabel(), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					onPositiveButton();
				}
			})
		).create();
	}
	
	protected void onNegativeButton() {
		if (host != null) {
			host.onDialogNegativeButton();
		}
	}

	protected void onPositiveButton() {
		if (this.host != null) {
			host.onDialogPositiveButton();
		}
	}

}
